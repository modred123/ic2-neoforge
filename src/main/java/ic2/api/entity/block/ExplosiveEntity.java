/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$MovementEmission
 *  net.minecraft.world.entity.EntityDimensions
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.Nullable
 */
package ic2.api.entity.block;

import ic2.core.Ic2Explosion;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class ExplosiveEntity
extends Entity {
    private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(ExplosiveEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final int DEFAULT_FUSE = 80;
    public LivingEntity causingEntity;
    public float explosivePower = 4.0f;
    public int radiationRange = 0;
    public float dropRate = 0.3f;
    public float damageVsEntities = 1.0f;
    public BlockState renderBlockState;

    public ExplosiveEntity(EntityType<? extends Entity> entityType, Level level, double d, double d2, double d3, int n, float f, float f2, float f3, BlockState blockState, int n2) {
        this(entityType, level);
        this.absMoveTo(d, d2, d3);
        double d4 = Math.PI * 2 * level.random.nextDouble();
        this.setDeltaMovement(-Math.sin(d4) * 0.02, 0.2, -Math.cos(d4) * 0.02);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
        this.setFuse(n);
        this.explosivePower = f;
        this.radiationRange = n2;
        this.dropRate = f2;
        this.damageVsEntities = f3;
        this.renderBlockState = blockState;
    }

    public ExplosiveEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(FUSE, 80);
    }

    public boolean canBeCollidedWith() {
        return !this.isRemoved();
    }

    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }
        int n = this.getFuse() - 1;
        this.setFuse(n);
        if (n <= 0) {
            this.discard();
            if (!this.level().isClientSide) {
                this.explode();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide) {
                this.level().addParticle((ParticleOptions)ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        Ic2Explosion ic2Explosion = new Ic2Explosion(this.getCommandSenderWorld(), this, this.getX(), this.getY(), this.getZ(), this.explosivePower, this.dropRate, this.radiationRange > 0 ? Ic2Explosion.Type.Nuclear : Ic2Explosion.Type.Normal, this.causingEntity, this.radiationRange);
        ic2Explosion.doExplosion();
    }

    @Nullable
    public LivingEntity getCausingEntity() {
        return this.causingEntity;
    }

    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    public boolean isPickable() {
        return !this.isRemoved();
    }

    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putShort("Fuse", (short)this.getFuse());
    }

    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.setFuse(compoundTag.getShort("Fuse"));
    }

    protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.15f;
    }

    public void setFuse(int n) {
        this.entityData.set(FUSE, n);
    }

    public int getFuse() {
        return (Integer)this.entityData.get(FUSE);
    }

    public ExplosiveEntity setCausingEntity(LivingEntity livingEntity) {
        this.causingEntity = livingEntity;
        return this;
    }
}

