/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.audio;

import ic2.core.audio.PositionSpec;
import ic2.core.util.Util;
import java.lang.ref.WeakReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class AudioPosition {
    private final WeakReference<Level> worldRef;
    public final float x;
    public final float y;
    public final float z;

    public static AudioPosition getFrom(Object object, PositionSpec positionSpec) {
        if (object instanceof AudioPosition) {
            return (AudioPosition)object;
        }
        if (object instanceof Entity) {
            Entity entity = (Entity)object;
            return new AudioPosition(entity.getCommandSenderWorld(), (float)entity.getX(), (float)entity.getY(), (float)entity.getZ());
        }
        if (object instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity)object;
            return new AudioPosition(blockEntity.getLevel(), (float)blockEntity.getBlockPos().getX() + 0.5f, (float)blockEntity.getBlockPos().getY() + 0.5f, (float)blockEntity.getBlockPos().getZ() + 0.5f);
        }
        return null;
    }

    public AudioPosition(Level level, float f, float f2, float f3) {
        this.worldRef = new WeakReference<Level>(level);
        this.x = f;
        this.y = f2;
        this.z = f3;
    }

    public AudioPosition(Level level, BlockPos blockPos) {
        this(level, (float)blockPos.getX() + 0.5f, (float)blockPos.getY() + 0.5f, (float)blockPos.getZ() + 0.5f);
    }

    public Level getWorld() {
        return (Level)this.worldRef.get();
    }

    public Vec3 asVec3d() {
        return new Vec3((double)this.x, (double)this.y, (double)this.z);
    }

    public boolean isSameBlock(BlockPos blockPos) {
        return blockPos.getX() == Util.roundToNegInf(this.x) && blockPos.getY() == Util.roundToNegInf(this.y) && blockPos.getZ() == Util.roundToNegInf(this.z);
    }
}

