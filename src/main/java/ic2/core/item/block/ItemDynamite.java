/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.BlockDispenser
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.entity.block.ITntEntity;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class ItemDynamite
extends ItemIC2
implements IBoxable {
    public boolean sticky;

    public ItemDynamite(ItemName name) {
        super(name);
        this.sticky = name == ItemName.dynamite_sticky;
        this.setMaxStackSize(16);
        DispenserBlock.registerBehavior(this, new DispenseItemBehavior(){
            public ItemStack dispense(BlockSource source, ItemStack stack) {
                Level level = source.level();
                double x = source.pos().getX() + 0.5;
                double y = source.pos().getY() + 0.5;
                double z = source.pos().getZ() + 0.5;
                ITntEntity dynamite = new ITntEntity(level, x, y, z);
                dynamite.setDeltaMovement(source.state().getValue(DispenserBlock.FACING).getStepX() * 0.7, 0.1, source.state().getValue(DispenserBlock.FACING).getStepZ() * 0.7);
                level.addFreshEntity(dynamite);
                stack.shrink(1);
                return stack;
            }
        });
    }

    public int getMetadata(int i) {
        return i;
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        return this.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (!player.getAbilities().instabuild) {
            stack = StackUtil.decSize(stack);
        }
        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        if (IC2.platform.isSimulating()) {
            ITntEntity dynamite = new ITntEntity(world, player.getX(), player.getEyeY() - 0.1, player.getZ());
            dynamite.setDeltaMovement(player.getViewVector(1.0F).scale(1.5F));
            dynamite.setCausingEntity(player);
            world.addFreshEntity(dynamite);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemstack) {
        return true;
    }
}

