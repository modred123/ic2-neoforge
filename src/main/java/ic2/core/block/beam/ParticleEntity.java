/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundAddEntityPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.level.Level
 */
package ic2.core.block.beam;

import ic2.core.block.beam.TileEntityEmitter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;

public class ParticleEntity
extends Entity {
    private static final double initialVelocity = 0.5;
    private static final double slowdown = 0.99;

    public ParticleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public ParticleEntity(TileEntityEmitter tileEntityEmitter) {
        this(null, tileEntityEmitter.getLevel());
        Direction direction = tileEntityEmitter.getFacing();
        double d = (double)tileEntityEmitter.getBlockPos().getX() + 0.5 + (double)direction.getStepX() * 0.5;
        double d2 = (double)tileEntityEmitter.getBlockPos().getY() + 0.5 + (double)direction.getStepY() * 0.5;
        double d3 = (double)tileEntityEmitter.getBlockPos().getZ() + 0.5 + (double)direction.getStepZ() * 0.5;
        this.absMoveTo(d, d2, d3);
        this.setDeltaMovement((double)direction.getStepX() * 0.5, (double)direction.getStepY() * 0.5, (double)direction.getStepZ() * 0.5);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    public void tick() {
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.99));
        if (this.getDeltaMovement().lengthSqr() < 1.0E-4) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}

