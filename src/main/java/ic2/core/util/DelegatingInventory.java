/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.util;

import java.util.Set;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DelegatingInventory
implements Container {
    private final Container parent;

    public DelegatingInventory(Container container) {
        this.parent = container;
    }

    public void clearContent() {
        this.parent.clearContent();
    }

    public int getContainerSize() {
        return this.parent.getContainerSize();
    }

    public boolean isEmpty() {
        return this.parent.isEmpty();
    }

    public ItemStack getItem(int n) {
        return this.parent.getItem(n);
    }

    public ItemStack removeItem(int n, int n2) {
        return this.parent.removeItem(n, n2);
    }

    public ItemStack removeItemNoUpdate(int n) {
        return this.parent.removeItemNoUpdate(n);
    }

    public void setItem(int n, ItemStack itemStack) {
        this.parent.setItem(n, itemStack);
    }

    public int getMaxStackSize() {
        return this.parent.getMaxStackSize();
    }

    public void setChanged() {
        this.parent.setChanged();
    }

    public boolean stillValid(Player player) {
        return this.parent.stillValid(player);
    }

    public void startOpen(Player player) {
        this.parent.startOpen(player);
    }

    public void stopOpen(Player player) {
        this.parent.stopOpen(player);
    }

    public boolean canPlaceItem(int n, ItemStack itemStack) {
        return this.parent.canPlaceItem(n, itemStack);
    }

    public int countItem(Item item) {
        return this.parent.countItem(item);
    }

    public boolean hasAnyOf(Set<Item> set) {
        return this.parent.hasAnyOf(set);
    }
}

