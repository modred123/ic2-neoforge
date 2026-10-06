/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.SnowLayerBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.item.tfbp.TerraformerBase;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class Chilling
extends TerraformerBase {
    @Override
    boolean terraform(Level level, BlockPos blockPos) {
        if ((blockPos = TileEntityTerra.getFirstBlockFrom(level, blockPos, 10)) == null) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block == Blocks.WATER) {
            level.setBlockAndUpdate(blockPos, Blocks.ICE.defaultBlockState());
            return true;
        }
        if (block == Blocks.ICE) {
            BlockPos blockPos2 = blockPos.below();
            Block block2 = level.getBlockState(blockPos2).getBlock();
            if (block2 == Blocks.WATER) {
                level.setBlockAndUpdate(blockPos2, Blocks.ICE.defaultBlockState());
                return true;
            }
        } else if (block == Blocks.SNOW) {
            if (Chilling.isSurroundedBySnow(level, blockPos)) {
                level.setBlockAndUpdate(blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
                return true;
            }
            int n = (Integer)blockState.getValue((Property)SnowLayerBlock.LAYERS);
            if (SnowLayerBlock.LAYERS.getPossibleValues().contains(n + 1)) {
                level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue((Property)SnowLayerBlock.LAYERS, Integer.valueOf(n + 1)));
                return true;
            }
        }
        blockPos = blockPos.above();
        if (Blocks.SNOW.defaultBlockState().canSurvive((LevelReader)level, blockPos) || block == Blocks.ICE) {
            level.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState());
            return true;
        }
        return false;
    }

    private static boolean isSurroundedBySnow(Level level, BlockPos blockPos) {
        for (Direction direction : Util.HORIZONTAL_DIRS) {
            if (Chilling.isSnowHere(level, blockPos.relative(direction))) continue;
            return false;
        }
        return true;
    }

    private static boolean isSnowHere(Level level, BlockPos blockPos) {
        int n = blockPos.getY();
        if ((blockPos = TileEntityTerra.getFirstBlockFrom(level, blockPos, 16)) == null || n > blockPos.getY()) {
            return false;
        }
        Block block = level.getBlockState(blockPos).getBlock();
        if (block == Blocks.SNOW_BLOCK || block == Blocks.SNOW) {
            return true;
        }
        blockPos = blockPos.above();
        if (Blocks.SNOW.defaultBlockState().canSurvive((LevelReader)level, blockPos) || block == Blocks.ICE) {
            level.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState());
        }
        return false;
    }
}

