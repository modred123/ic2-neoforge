/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.core.Direction
 */
package ic2.api.transport;

import ic2.api.transport.IPipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

public interface IItemTransportTile
extends IPipe {
    public int putItems(ItemStack var1, Direction var2, boolean var3);

    public ItemStack getContents();

    public void setContents(ItemStack var1);

    public int getMaxStackSizeAllowed();

    public int getTransferRate();
}

