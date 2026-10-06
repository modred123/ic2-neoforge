/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemBlock
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.tileentity.TileEntityInventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableBlock
extends InvSlotConsumable {
    public InvSlotConsumableBlock(TileEntityInventory base1, String name1, int count) {
        this(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.TOP);
    }

    public InvSlotConsumableBlock(TileEntityInventory base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1) {
        super(base1, name1, access1, count, preferredSide1);
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof BlockItem;
    }
}

