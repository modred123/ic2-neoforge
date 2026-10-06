/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SlotCustom
extends Slot {
    private final Item item;

    public SlotCustom(Container iinventory, Item item, int i, int j, int k) {
        super(iinventory, i, j, k);
        this.item = item;
    }

    public boolean isItemValid(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        }
        return this.item != null && itemstack.getItem() == this.item;
    }
}

