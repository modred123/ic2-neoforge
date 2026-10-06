/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.level.Level
 */
package ic2.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

public class ParticleUtil {
    public static void showFurnaceFlames(Level level, BlockPos blockPos, Direction direction) {
        if (level.random.nextInt(8) != 0) {
            return;
        }
        double d = (double)blockPos.getX() + ((double)direction.getStepX() * 1.04 + 1.0) / 2.0;
        double d2 = (double)blockPos.getY() + (double)level.random.nextFloat() * 0.375;
        double d3 = (double)blockPos.getZ() + ((double)direction.getStepZ() * 1.04 + 1.0) / 2.0;
        if (direction.getAxis() == Direction.Axis.X) {
            d3 += (double)level.random.nextFloat() * 0.625 - 0.3125;
        } else {
            d += (double)level.random.nextFloat() * 0.625 - 0.3125;
        }
        level.addParticle((ParticleOptions)ParticleTypes.SMOKE, d, d2, d3, 0.0, 0.0, 0.0);
        level.addParticle((ParticleOptions)ParticleTypes.FLAME, d, d2, d3, 0.0, 0.0, 0.0);
    }
}

