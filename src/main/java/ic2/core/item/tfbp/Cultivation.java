/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.CropBlock
 *  net.minecraft.world.level.block.DirectionalBlock
 *  net.minecraft.world.level.block.DoublePlantBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.DoubleBlockHalf
 *  net.minecraft.world.level.block.state.properties.Property
 */
package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.item.tfbp.TerraformerBase;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

public class Cultivation
extends TerraformerBase {
    static List<BlockState> plants = new ArrayList<BlockState>();

    @Override
    void init() {
        plants.add(Blocks.SHORT_GRASS.defaultBlockState());
        plants.add(Blocks.SHORT_GRASS.defaultBlockState());
        plants.add(Blocks.FERN.defaultBlockState());
        plants.add(Blocks.POPPY.defaultBlockState());
        plants.add(Blocks.DANDELION.defaultBlockState());
        plants.add(Blocks.SHORT_GRASS.defaultBlockState());
        plants.add(Blocks.ROSE_BUSH.defaultBlockState());
        plants.add(Blocks.SUNFLOWER.defaultBlockState());
        for (Holder holder : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.SAPLINGS)) {
            Block block = (Block)holder.value();
            if (!Cultivation.isVanilla(block)) continue;
            plants.add(block.defaultBlockState());
        }
        plants.add(Blocks.WHEAT.defaultBlockState());
        plants.add(Blocks.RED_MUSHROOM.defaultBlockState());
        plants.add(Blocks.BROWN_MUSHROOM.defaultBlockState());
        plants.add(Blocks.PUMPKIN.defaultBlockState());
        plants.add(Blocks.MELON.defaultBlockState());
        plants.add(Ic2Blocks.RUBBER_SAPLING.defaultBlockState());
    }

    @Override
    boolean terraform(Level level, BlockPos blockPos) {
        Block block;
        if ((blockPos = TileEntityTerra.getFirstSolidBlockFrom(level, blockPos, 10)) == null) {
            return false;
        }
        if (TileEntityTerra.switchGround(level, blockPos, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true)) {
            return true;
        }
        if (TileEntityTerra.switchGround(level, blockPos, Blocks.END_STONE, Blocks.DIRT.defaultBlockState(), true)) {
            int n = 4;
            while (--n > 0 && TileEntityTerra.switchGround(level, blockPos, Blocks.END_STONE, Blocks.DIRT.defaultBlockState(), true)) {
            }
        }
        if ((block = level.getBlockState(blockPos).getBlock()) == Blocks.DIRT) {
            level.setBlockAndUpdate(blockPos, Blocks.GRASS_BLOCK.defaultBlockState());
            return true;
        }
        if (block == Blocks.GRASS_BLOCK) {
            return Cultivation.growPlantsOn(level, blockPos);
        }
        return false;
    }

    private static boolean growPlantsOn(Level level, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.above();
        BlockState blockState = level.getBlockState(blockPos2);
        Block block = blockState.getBlock();
        if (blockState.isAir() || block == Blocks.SHORT_GRASS && level.random.nextInt(4) == 0) {
            BlockState blockState2 = Cultivation.pickRandomPlant(level.random);
            if (blockState2.getValues().containsKey((Object)DirectionalBlock.FACING)) {
                blockState2 = (BlockState)blockState2.setValue((Property)DirectionalBlock.FACING, Util.HORIZONTAL_DIRS[level.random.nextInt(Util.HORIZONTAL_DIRS.length)]);
            }
            if (blockState2.getBlock() instanceof CropBlock) {
                level.setBlockAndUpdate(blockPos, Blocks.FARMLAND.defaultBlockState());
            } else if (blockState2.getBlock() instanceof DoublePlantBlock) {
                level.setBlockAndUpdate(blockPos2, (BlockState)blockState2.setValue((Property)DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
                level.setBlockAndUpdate(blockPos2.above(), (BlockState)blockState2.setValue((Property)DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
                return true;
            }
            level.setBlockAndUpdate(blockPos2, blockState2);
            return true;
        }
        return false;
    }

    private static BlockState pickRandomPlant(RandomSource randomSource) {
        return plants.get(randomSource.nextInt(plants.size()));
    }
}

