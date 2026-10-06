/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntitySelector
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.animal.WaterAnimal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.vehicle.Boat
 *  net.minecraft.world.entity.vehicle.Boat$Status
 *  net.minecraft.world.entity.vehicle.Boat$Type
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package ic2.api.entity.boat;

import ic2.api.entity.boat.BoatType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractBoatEntity
extends Boat {
    protected boolean isExtraItemDropped = false;

    public AbstractBoatEntity(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
    }

    public AbstractBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level level, double d, double d2, double d3) {
        this(entityType, level);
        this.setPos(d, d2, d3);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
    }

    public void setVariant(Boat.Type type) {
        super.setVariant(Boat.Type.OAK);
    }

    public ItemStack getExtraDropItemStack() {
        return ItemStack.EMPTY;
    }

    public abstract BoatType getOverrideBoatType();

    public boolean brokenByFalling() {
        return true;
    }

    public boolean canFloatOn(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    protected SoundEvent getPaddleSound() {
        switch (this.checkLocation()) {
            case IN_WATER: 
            case UNDER_WATER: 
            case UNDER_FLOWING_WATER: {
                return SoundEvents.BOAT_PADDLE_WATER;
            }
            case ON_LAND: {
                return SoundEvents.BOAT_PADDLE_LAND;
            }
        }
        return null;
    }

    private Boat.Status checkLocation() {
        Class<Boat> clazz = Boat.class;
        try {
            Field field = clazz.getDeclaredField("waterLevel");
            Field field2 = clazz.getDeclaredField("nearbySlipperiness");
            field.setAccessible(true);
            field2.setAccessible(true);
            Boat.Status status = this.getUnderWaterLocation();
            if (status != null) {
                field.set((Object)this, this.getBoundingBox().maxY);
                return status;
            }
            if (this.checkBoatInWater()) {
                return Boat.Status.IN_WATER;
            }
            float f = this.getGroundFriction();
            if (f > 0.0f) {
                field2.set((Object)this, Float.valueOf(f));
                return Boat.Status.ON_LAND;
            }
            return Boat.Status.IN_AIR;
        }
        catch (IllegalAccessException | NoSuchFieldException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    private boolean checkBoatInWater() {
        Class<Boat> clazz = Boat.class;
        try {
            Field field = clazz.getDeclaredField("waterLevel");
            field.setAccessible(true);
            AABB aABB = this.getBoundingBox();
            int n = Mth.floor((double)aABB.minX);
            int n2 = Mth.ceil((double)aABB.maxX);
            int n3 = Mth.floor((double)aABB.minY);
            int n4 = Mth.ceil((double)(aABB.minY + 0.001));
            int n5 = Mth.floor((double)aABB.minZ);
            int n6 = Mth.ceil((double)aABB.maxZ);
            boolean bl = false;
            field.set((Object)this, -1.7976931348623157E308);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int i = n; i < n2; ++i) {
                for (int j = n3; j < n4; ++j) {
                    for (int k = n5; k < n6; ++k) {
                        mutableBlockPos.set(i, j, k);
                        FluidState fluidState = this.level().getFluidState((BlockPos)mutableBlockPos);
                        if (!this.canFloatOn(fluidState)) continue;
                        float f = (float)j + fluidState.getHeight((BlockGetter)this.level(), (BlockPos)mutableBlockPos);
                        field.set((Object)this, Math.max((double)f, (Double)field.get((Object)this)));
                        bl |= aABB.minY < (double)f;
                    }
                }
            }
            return bl;
        }
        catch (IllegalAccessException | NoSuchFieldException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    private Boat.Status getUnderWaterLocation() {
        AABB aABB = this.getBoundingBox();
        double d = aABB.maxY + 0.001;
        int n = Mth.floor((double)aABB.minX);
        int n2 = Mth.ceil((double)aABB.maxX);
        int n3 = Mth.floor((double)aABB.maxY);
        int n4 = Mth.ceil((double)d);
        int n5 = Mth.floor((double)aABB.minZ);
        int n6 = Mth.ceil((double)aABB.maxZ);
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = n; i < n2; ++i) {
            for (int j = n3; j < n4; ++j) {
                for (int k = n5; k < n6; ++k) {
                    mutableBlockPos.set(i, j, k);
                    FluidState fluidState = this.level().getFluidState((BlockPos)mutableBlockPos);
                    if (!this.canFloatOn(fluidState) || !(d < (double)((float)mutableBlockPos.getY() + fluidState.getHeight((BlockGetter)this.level(), (BlockPos)mutableBlockPos)))) continue;
                    if (fluidState.isSource()) {
                        bl = true;
                        continue;
                    }
                    return Boat.Status.UNDER_FLOWING_WATER;
                }
            }
        }
        return bl ? Boat.Status.UNDER_WATER : null;
    }

    public float getWaterLevelAbove() {
        Class<Boat> clazz = Boat.class;
        try {
            Field field = clazz.getDeclaredField("fallVelocity");
            field.setAccessible(true);
            AABB aABB = this.getBoundingBox();
            int n = Mth.floor((double)aABB.minX);
            int n2 = Mth.ceil((double)aABB.maxX);
            int n3 = Mth.floor((double)aABB.maxY);
            int n4 = Mth.ceil((double)(aABB.maxY - (Double)field.get((Object)this)));
            int n5 = Mth.floor((double)aABB.minZ);
            int n6 = Mth.ceil((double)aABB.maxZ);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            block2: for (int i = n3; i < n4; ++i) {
                float f = 0.0f;
                for (int j = n; j < n2; ++j) {
                    for (int k = n5; k < n6; ++k) {
                        mutableBlockPos.set(j, i, k);
                        FluidState fluidState = this.level().getFluidState((BlockPos)mutableBlockPos);
                        if (this.canFloatOn(fluidState)) {
                            f = Math.max(f, fluidState.getHeight((BlockGetter)this.level(), (BlockPos)mutableBlockPos));
                        }
                        if (f >= 1.0f) continue block2;
                    }
                }
                if (!(f < 1.0f)) continue;
                return (float)mutableBlockPos.getY() + f;
            }
            return n4 + 1;
        }
        catch (IllegalAccessException | NoSuchFieldException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    private void invokePrivateMethod(Class<Boat> clazz, String string, Object ... objectArray) {
        try {
            Class[] classArray = new Class[objectArray.length];
            for (int i = 0; i < objectArray.length; ++i) {
                classArray[i] = objectArray[i].getClass();
            }
            Method method = clazz.getDeclaredMethod(string, classArray);
            method.setAccessible(true);
            method.invoke((Object)this, objectArray);
        }
        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    public void tick() {
        Class<Boat> clazz = Boat.class;
        try {
            Field field = clazz.getDeclaredField("lastLocation");
            Field field2 = clazz.getDeclaredField("location");
            Field field3 = clazz.getDeclaredField("ticksUnderwater");
            Field field4 = clazz.getDeclaredField("paddlePhases");
            field.setAccessible(true);
            field2.setAccessible(true);
            field3.setAccessible(true);
            field4.setAccessible(true);
            field.set((Object)this, field2.get((Object)this));
            field2.set((Object)this, this.checkLocation());
            Boat.Status status = (Boat.Status)field2.get((Object)this);
            float f = ((Float)field3.get((Object)this)).floatValue() + 1.0f;
            float[] fArray = (float[])field4.get((Object)this);
            field3.set((Object)this, Float.valueOf(status == Boat.Status.UNDER_WATER || status == Boat.Status.UNDER_FLOWING_WATER ? f : 0.0f));
            float f2 = ((Float)field3.get((Object)this)).floatValue();
            if (!this.level().isClientSide && f2 >= 60.0f) {
                this.ejectPassengers();
            }
            if (this.getHurtTime() > 0) {
                this.setHurtTime(this.getHurtTime() - 1);
            }
            if (this.getDamage() > 0.0f) {
                this.setDamage(this.getDamage() - 1.0f);
            }
            this.baseTick();
            this.invokePrivateMethod(clazz, "updatePositionAndRotation", new Object[0]);
            if (this.isControlledByLocalInstance()) {
                if (!(this.getFirstPassenger() instanceof Player)) {
                    this.setPaddleState(false, false);
                }
                this.invokePrivateMethod(clazz, "updateVelocity", new Object[0]);
                if (this.level().isClientSide) {
                    this.invokePrivateMethod(clazz, "updatePaddles", new Object[0]);
                    this.level().sendPacketToServer((Packet)new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
                }
                this.move(MoverType.SELF, this.getDeltaMovement());
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }
            this.invokePrivateMethod(clazz, "handleBubbleColumn", new Object[0]);
            for (int i = 0; i <= 1; ++i) {
                if (this.getPaddleState(i)) {
                    SoundEvent soundEvent;
                    if (!this.isSilent() && (double)(fArray[i] % ((float)Math.PI * 2)) <= 0.7853981852531433 && (double)((fArray[i] + 0.3926991f) % ((float)Math.PI * 2)) >= 0.7853981852531433 && (soundEvent = this.getPaddleSound()) != null) {
                        Vec3 vec3 = this.getViewVector(1.0f);
                        double d = i == 1 ? -vec3.z : vec3.z;
                        double d2 = i == 1 ? vec3.x : -vec3.x;
                        this.level().playSound(null, this.getX() + d, this.getY(), this.getZ() + d2, soundEvent, this.getSoundSource(), 1.0f, 0.8f + 0.4f * this.random.nextFloat());
                    }
                    fArray[i] = fArray[i] + 0.3926991f;
                    continue;
                }
                fArray[i] = 0.0f;
            }
            field4.set((Object)this, fArray);
            this.checkInsideBlocks();
            List<Entity> list = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)0.2f, (double)-0.01f, (double)0.2f), EntitySelector.pushableBy((Entity)this));
            if (!list.isEmpty()) {
                boolean bl = !this.level().isClientSide && !(this.getControllingPassenger() instanceof Player);
                for (Entity entity : list) {
                    if (entity.hasPassenger((Entity)this)) continue;
                    if (bl && this.getPassengers().size() < this.getMaxPassengers() && !entity.isPassenger() && entity.getBbWidth() < this.getBbWidth() && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player)) {
                        entity.startRiding((Entity)this);
                        continue;
                    }
                    this.push(entity);
                }
            }
        }
        catch (IllegalAccessException | NoSuchFieldException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        if (bl && this.brokenByFalling()) {
            super.checkFallDamage(d, true, blockState, blockPos);
            return;
        }
        if (!bl) {
            super.checkFallDamage(d, false, blockState, blockPos);
        }
    }

    public ItemEntity spawnAtLocation(ItemLike itemLike) {
        if (itemLike == this.getVariant().getPlanks()) {
            return this.spawnAtLocation(new ItemStack((ItemLike)this.getOverrideBoatType().getBaseItem()));
        }
        if (itemLike == Items.STICK && !this.isExtraItemDropped) {
            this.isExtraItemDropped = true;
            return this.spawnAtLocation(this.getExtraDropItemStack());
        }
        return super.spawnAtLocation(itemLike);
    }

    public void resetFallDistance() {
        super.resetFallDistance();
        this.isExtraItemDropped = false;
    }
}

