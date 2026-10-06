/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.slot;

import ic2.api.item.ItemWrapper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotBoxable
extends Slot {
    public SlotBoxable(Container iinventory, int i, int j, int k) {
        super(iinventory, i, j, k);
    }

    public boolean isItemValid(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        }
        return ItemWrapper.canBeStoredInToolbox(itemstack);
    }
}

