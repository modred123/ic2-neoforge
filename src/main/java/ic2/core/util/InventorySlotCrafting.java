/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.util;

import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public abstract class InventorySlotCrafting
implements CraftingContainer {
    protected final int width;
    protected final int height;
    protected final int size;

    public InventorySlotCrafting(int width, int height) {
        this.width = width;
        this.height = height;
        this.size = width * height;
    }

    protected boolean validIndex(int index) {
        return index >= 0 && index < this.size;
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    public ItemStack getStackInRowAndColumn(int row, int column) {
        return row >= 0 && row < this.height && column >= 0 && column < this.width ? this.getItem(row + column * this.height) : StackUtil.emptyStack;
    }

    protected abstract ItemStack get(int var1);

    protected abstract void put(int var1, ItemStack var2);

    protected void clear(int index) {
        this.put(index, StackUtil.emptyStack);
    }

    @Override
    public ItemStack getItem(int index) {
        return !this.validIndex(index) ? StackUtil.emptyStack : this.get(index);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (this.validIndex(index)) {
            ItemStack stack = this.get(index);
            this.clear(index);
            return stack;
        }
        return StackUtil.emptyStack;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack stack;
        if (this.validIndex(index) && !StackUtil.isEmpty(stack = this.get(index))) {
            ItemStack ret;
            if (count >= StackUtil.getSize(stack)) {
                ret = stack;
                this.clear(index);
            } else {
                ret = StackUtil.copyWithSize(stack, count);
                this.put(index, StackUtil.decSize(stack, count));
            }
            return ret;
        }
        return StackUtil.emptyStack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (this.validIndex(index)) {
            this.put(index, stack);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public List<ItemStack> getItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>(this.size);
        for (int i = 0; i < this.size; ++i) {
            ret.add(this.get(i));
        }
        return ret;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.size; ++i) {
            this.clear(i);
        }
    }

    @Override
    public abstract boolean isEmpty();

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}

