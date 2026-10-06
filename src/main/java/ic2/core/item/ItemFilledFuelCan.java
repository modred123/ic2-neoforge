/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item;

import ic2.core.item.ItemIC2;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;

public class ItemFilledFuelCan
extends ItemIC2 {
    public ItemFilledFuelCan() {
        super(ItemName.filled_fuel_can);
        this.setMaxStackSize(1);
    }

    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    public ItemStack getContainerItem(ItemStack stack) {
        return ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can);
    }

    public int getItemBurnTime(ItemStack stack) {
        return StackUtil.getOrCreateNbtData(stack).getInt("value") * 2;
    }
}

