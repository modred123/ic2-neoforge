/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntitySelector
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.vehicle.Boat
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.item.boat;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoatItem
extends Item {
    private static final Predicate<Entity> RIDERS = EntitySelector.NO_SPECTATORS.and((Predicate<Entity>)Entity::isPickable);
    private final Class<? extends Boat> boatEntityClass;
    private final EntityType<? extends Boat> boatEntityType;

    public BoatItem(Class<? extends Boat> clazz, EntityType<? extends Boat> entityType, Item.Properties properties) {
        super(properties);
        this.boatEntityClass = clazz;
        this.boatEntityType = entityType;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = BoatItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        Vec3 viewVector = player.getViewVector(1.0f);
        List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(viewVector.scale(5.0)).inflate(1.0), RIDERS);
        if (!list.isEmpty()) {
            Vec3 eyePosition = player.getEyePosition();
            for (Entity entity : list) {
                AABB aABB = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                if (!aABB.contains(eyePosition)) continue;
                return InteractionResultHolder.pass(itemStack);
            }
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            Boat boat = this.createEntity(level, blockHitResult);
            boat.setYRot(player.getYRot());
            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResultHolder.fail(itemStack);
            }
            if (!level.isClientSide) {
                level.addFreshEntity(boat);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, blockHitResult.getLocation());
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }
        return InteractionResultHolder.pass(itemStack);
    }

    private Boat createEntity(Level level, HitResult hitResult) {
        if (this.boatEntityClass == null) {
            IC2.log.error(LogCategory.General, "Boat entity class doesn't exist");
            return null;
        }
        try {
            Constructor<? extends Boat> constructor = this.boatEntityClass.getConstructor(EntityType.class, Level.class, Double.TYPE, Double.TYPE, Double.TYPE);
            return constructor.newInstance(this.boatEntityType, level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }
}

