/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.core.Direction
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.block;

import ic2.core.item.block.ItemBlockIC2;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemLuminator
extends ItemBlockIC2 {
    public ItemLuminator(Block block) {
        super(block);
    }

    @Override
    protected boolean placeBlock(net.minecraft.world.item.context.BlockPlaceContext context, BlockState state) {
        return context.getLevel().setBlockAndUpdate(context.getClickedPos(), state);
    }
}

