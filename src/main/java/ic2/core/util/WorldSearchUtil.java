/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.chunk.Chunk
 */
package ic2.core.util;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class WorldSearchUtil {
    public static void findTileEntities(Level world, BlockPos center, int range, ITileEntityResultHandler handler) {
        int minX = center.getX() - range;
        int minY = center.getY() - range;
        int minZ = center.getZ() - range;
        int maxX = center.getX() + range;
        int maxY = center.getY() + range;
        int maxZ = center.getZ() + range;
        int xS = minX >> 4;
        int zS = minZ >> 4;
        int xE = maxX >> 4;
        int zE = maxZ >> 4;
        for (int x = xS; x <= xE; ++x) {
            for (int z = zS; z <= zE; ++z) {
                LevelChunk chunk = world.getChunk(x, z);
                for (BlockEntity te : chunk.getBlockEntities().values()) {
                    BlockPos pos = te.getBlockPos();
                    if (pos.getY() < minY || pos.getY() > maxY || pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ || !handler.onMatch(te)) continue;
                    return;
                }
            }
        }
    }

    public static interface ITileEntityResultHandler {
        public boolean onMatch(BlockEntity var1);
    }
}

