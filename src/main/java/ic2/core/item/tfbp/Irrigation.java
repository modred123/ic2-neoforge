/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.BonemealableBlock
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.item.tfbp.TerraformerBase;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Irrigation
extends TerraformerBase {
    @Override
    boolean terraform(Level level, BlockPos blockPos) {
        if (level.random.nextInt(48000) == 0) {
            level.getLevelData().setRaining(true);
            return true;
        }
        if ((blockPos = TileEntityTerra.getFirstBlockFrom(level, blockPos, 10)) == null) {
            return false;
        }
        if (TileEntityTerra.switchGround(level, blockPos, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true)) {
            TileEntityTerra.switchGround(level, blockPos, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true);
            return true;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof BonemealableBlock && ((BonemealableBlock)block).isValidBonemealTarget(level, blockPos, blockState)) {
            ((BonemealableBlock)block).performBonemeal((ServerLevel)level, level.random, blockPos, blockState);
            return true;
        }
        if (block == Blocks.SHORT_GRASS) {
            return Irrigation.spreadGrass(level, blockPos.north()) || Irrigation.spreadGrass(level, blockPos.east()) || Irrigation.spreadGrass(level, blockPos.south()) || Irrigation.spreadGrass(level, blockPos.west());
        }
        if (blockState.is(BlockTags.LOGS)) {
            BlockPos blockPos2 = blockPos.above();
            level.setBlockAndUpdate(blockPos2, blockState);
            BlockState blockState2 = Irrigation.getLeaves(level, blockPos);
            if (blockState2 != null) {
                Irrigation.createLeaves(level, blockPos2, blockState2);
            }
            return true;
        }
        if (block == Blocks.FIRE) {
            level.removeBlock(blockPos, false);
            return true;
        }
        return false;
    }

    private static BlockState getLeaves(Level level, BlockPos blockPos) {
        for (Direction direction : Util.HORIZONTAL_DIRS) {
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos2);
            if (!blockState.is(BlockTags.LEAVES)) continue;
            return blockState;
        }
        return null;
    }

    private static void createLeaves(Level level, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos2 = blockPos.above();
        if (level.isEmptyBlock(blockPos2)) {
            level.setBlockAndUpdate(blockPos2, blockState);
        }
        for (Direction direction : Util.HORIZONTAL_DIRS) {
            BlockPos blockPos3 = blockPos.relative(direction);
            if (!level.isEmptyBlock(blockPos3)) continue;
            level.setBlockAndUpdate(blockPos3, blockState);
        }
    }

    private static boolean spreadGrass(Level level, BlockPos blockPos) {
        if (level.random.nextBoolean()) {
            return false;
        }
        if ((blockPos = TileEntityTerra.getFirstBlockFrom(level, blockPos, 0)) == null) {
            return false;
        }
        Block block = level.getBlockState(blockPos).getBlock();
        if (block == Blocks.DIRT) {
            level.setBlockAndUpdate(blockPos, Blocks.GRASS_BLOCK.defaultBlockState());
            return true;
        }
        if (block == Blocks.GRASS_BLOCK) {
            level.setBlockAndUpdate(blockPos.above(), Blocks.SHORT_GRASS.defaultBlockState());
            return true;
        }
        return false;
    }
}

