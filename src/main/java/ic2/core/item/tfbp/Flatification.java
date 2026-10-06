/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 */
package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.item.tfbp.TerraformerBase;
import ic2.core.ref.Ic2Blocks;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class Flatification
extends TerraformerBase {
    static Set<Block> removable = Collections.newSetFromMap(new IdentityHashMap());

    @Override
    void init() {
        removable.add(Blocks.SNOW);
        removable.add(Blocks.ICE);
        removable.add(Blocks.GRASS_BLOCK);
        removable.add(Blocks.STONE);
        removable.add(Blocks.GRAVEL);
        removable.add(Blocks.SAND);
        removable.add(Blocks.DIRT);
        removable.add(Blocks.OAK_LEAVES);
        removable.add(Blocks.SPRUCE_LEAVES);
        removable.add(Blocks.BIRCH_LEAVES);
        removable.add(Blocks.JUNGLE_LEAVES);
        removable.add(Blocks.ACACIA_LEAVES);
        removable.add(Blocks.DARK_OAK_LEAVES);
        removable.add(Blocks.TALL_GRASS);
        removable.add(Blocks.POPPY);
        removable.add(Blocks.DANDELION);
        removable.add(Blocks.WHEAT);
        removable.add(Blocks.RED_MUSHROOM);
        removable.add(Blocks.BROWN_MUSHROOM);
        removable.add(Blocks.PUMPKIN);
        removable.add(Blocks.MELON);
        removable.add((Block)Ic2Blocks.RUBBER_LEAVES);
        removable.add(Ic2Blocks.RUBBER_SAPLING);
        removable.add((Block)Ic2Blocks.RUBBER_LOG);
    }

    @Override
    boolean terraform(Level level, BlockPos blockPos) {
        BlockPos blockPos2 = TileEntityTerra.getFirstBlockFrom(level, blockPos, 20);
        if (blockPos2 == null) {
            return false;
        }
        if (level.getBlockState(blockPos2).getBlock() == Blocks.SNOW) {
            blockPos2 = blockPos2.below();
        }
        if (blockPos.getY() == blockPos2.getY()) {
            return false;
        }
        if (blockPos2.getY() < blockPos.getY()) {
            level.setBlockAndUpdate(blockPos2.above(), Blocks.DIRT.defaultBlockState());
            return true;
        }
        if (Flatification.canRemove(level.getBlockState(blockPos2).getBlock())) {
            level.removeBlock(blockPos2, false);
            return true;
        }
        return false;
    }

    private static boolean canRemove(Block block) {
        return removable.contains(block) || block.builtInRegistryHolder().is(BlockTags.SAPLINGS) || block.builtInRegistryHolder().is(BlockTags.LOGS);
    }
}

