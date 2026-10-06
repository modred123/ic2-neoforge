/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.item.tfbp.Cultivation;
import ic2.core.item.tfbp.TerraformerBase;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Desertification
extends TerraformerBase {
    @Override
    boolean terraform(Level level, BlockPos blockPos) {
        if ((blockPos = TileEntityTerra.getFirstBlockFrom(level, blockPos, 10)) == null) {
            return false;
        }
        BlockState blockState = Blocks.SAND.defaultBlockState();
        if (TileEntityTerra.switchGround(level, blockPos, Blocks.DIRT, blockState, false) || TileEntityTerra.switchGround(level, blockPos, Blocks.GRASS_BLOCK, blockState, false) || TileEntityTerra.switchGround(level, blockPos, Blocks.FARMLAND, blockState, false)) {
            TileEntityTerra.switchGround(level, blockPos, Blocks.DIRT, blockState, false);
            return true;
        }
        BlockState blockState2 = level.getBlockState(blockPos);
        Block block = blockState2.getBlock();
        if (block == Blocks.WATER || block == Blocks.SNOW || blockState2.is(BlockTags.LEAVES) || Desertification.isPlant(block)) {
            level.removeBlock(blockPos, false);
            if (Desertification.isPlant(level.getBlockState(blockPos.above()).getBlock())) {
                level.removeBlock(blockPos.above(), false);
            }
            return true;
        }
        if (block == Blocks.ICE || block == Blocks.SNOW) {
            level.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        if ((blockState2.is(BlockTags.PLANKS) || blockState2.is(BlockTags.LOGS_THAT_BURN)) && level.random.nextInt(15) == 0) {
            level.setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
            return true;
        }
        return false;
    }

    private static boolean isPlant(Block block) {
        for (BlockState blockState : Cultivation.plants) {
            if (blockState.getBlock() != block) continue;
            return true;
        }
        return block.builtInRegistryHolder().is(BlockTags.SAPLINGS) || block.builtInRegistryHolder().is(BlockTags.CROPS) || block.builtInRegistryHolder().is(BlockTags.FLOWERS);
    }
}

