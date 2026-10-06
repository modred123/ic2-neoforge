/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.biome.Biomes
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.MushroomBlock
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.item.tfbp.TerraformerBase;
import ic2.core.util.BiomeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Mushroom
extends TerraformerBase {
    @Override
    boolean terraform(Level level, BlockPos blockPos) {
        if ((blockPos = TileEntityTerra.getFirstSolidBlockFrom(level, blockPos, 20)) == null) {
            return false;
        }
        return Mushroom.growBlockWithDependancy(level, blockPos, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM);
    }

    private static boolean growBlockWithDependancy(Level level, BlockPos blockPos, Block block, Block block2) {
        Block block3;
        Block block4;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = blockPos.getX() - 1; block2 != null && i < blockPos.getX() + 1; ++i) {
            block1: for (int j = blockPos.getZ() - 1; j < blockPos.getZ() + 1; ++j) {
                for (int k = blockPos.getY() + 5; k > blockPos.getY() - 2; --k) {
                    mutableBlockPos.set(i, k, j);
                    BlockState blockState4 = level.getBlockState(mutableBlockPos);
                    block3 = blockState4.getBlock();
                    if (block2 == Blocks.MYCELIUM) {
                        if (block3 == block2 || block3 == Blocks.BROWN_MUSHROOM_BLOCK || block3 == Blocks.RED_MUSHROOM_BLOCK) continue block1;
                        if (blockState4.isAir() || block3 != Blocks.DIRT && block3 != Blocks.GRASS_BLOCK) continue;
                        BlockPos blockPos2 = mutableBlockPos.immutable();
                        level.setBlockAndUpdate(blockPos2, block2.defaultBlockState());
                        BiomeUtil.setBiome((LevelReader)level, blockPos2, BiomeUtil.getBiome(level, (ResourceKey<Biome>)Biomes.MUSHROOM_FIELDS));
                        return true;
                    }
                    if (block2 != Blocks.BROWN_MUSHROOM) continue;
                    if (block3 == Blocks.BROWN_MUSHROOM || block3 == Blocks.RED_MUSHROOM) continue block1;
                    if (blockState4.isAir() || !Mushroom.growBlockWithDependancy(level, mutableBlockPos, Blocks.BROWN_MUSHROOM, Blocks.MYCELIUM)) continue;
                    return true;
                }
            }
        }
        if (block == Blocks.BROWN_MUSHROOM) {
            Block block5 = level.getBlockState(blockPos).getBlock();
            if (block5 != Blocks.MYCELIUM) {
                if (block5 == Blocks.BROWN_MUSHROOM_BLOCK || block5 == Blocks.RED_MUSHROOM_BLOCK) {
                    level.setBlockAndUpdate(blockPos, Blocks.MYCELIUM.defaultBlockState());
                } else {
                    return false;
                }
            }
            BlockPos blockPos3 = blockPos.above();
            BlockState blockState = level.getBlockState(blockPos3);
            block4 = blockState.getBlock();
            if (!blockState.isAir() && block4 != Blocks.SHORT_GRASS) {
                return false;
            }
            block3 = level.random.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
            level.setBlockAndUpdate(blockPos3, block3.defaultBlockState());
            return true;
        }
        if (block == Blocks.BROWN_MUSHROOM_BLOCK) {
            BlockPos blockPos4 = blockPos.above();
            BlockState blockState = level.getBlockState(blockPos4);
            Block block6 = blockState.getBlock();
            if (block6 != Blocks.BROWN_MUSHROOM && block6 != Blocks.RED_MUSHROOM) {
                return false;
            }
            if (((MushroomBlock)block6).growMushroom((ServerLevel)level, blockPos, blockState, level.random)) {
                for (int i = blockPos.getX() - 1; i < blockPos.getX() + 1; ++i) {
                    for (int j = blockPos.getZ() - 1; j < blockPos.getZ() + 1; ++j) {
                        mutableBlockPos.set(i, blockPos4.getY(), j);
                        Block block7 = level.getBlockState((BlockPos)mutableBlockPos).getBlock();
                        if (block7 != Blocks.BROWN_MUSHROOM && block7 != Blocks.RED_MUSHROOM) continue;
                        level.removeBlock(mutableBlockPos.immutable(), false);
                    }
                }
                return true;
            }
        }
        return false;
    }
}

