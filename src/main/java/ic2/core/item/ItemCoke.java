/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item;

import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import net.minecraft.world.item.ItemStack;

public class ItemCoke
extends ItemIC2 {
    public ItemCoke() {
        super(ItemName.coke);
        this.setMaxStackSize(64);
    }

    public int getItemBurnTime(ItemStack itemStack) {
        return 3200;
    }
}

