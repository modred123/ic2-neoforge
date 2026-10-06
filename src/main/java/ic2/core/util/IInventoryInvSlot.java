/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.Container
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.util;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IInventoryInvSlot
implements Container {
    public final InvSlot slot;

    public IInventoryInvSlot(InvSlot slot) {
        this.slot = slot;
    }

    @Override
    public int getContainerSize() {
        return this.slot.size();
    }

    @Override
    public int getMaxStackSize() {
        return this.slot.getStackSizeLimit();
    }

    @Override
    public boolean isEmpty() {
        return this.slot.isEmpty();
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return this.slot.accepts(stack);
    }

    @Override
    public ItemStack getItem(int index) {
        return this.slot.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack stack = this.getItem(index);
        if (!StackUtil.isEmpty(stack)) {
            int amount = Math.min(StackUtil.getSize(stack), count);
            ItemStack out = StackUtil.copyWithSize(stack, amount);
            this.setItem(index, StackUtil.decSize(stack, amount));
            return out;
        }
        return StackUtil.emptyStack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.slot.put(index, stack);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = this.getItem(index);
        this.setItem(index, StackUtil.emptyStack);
        return stack;
    }

    @Override
    public void clearContent() {
        this.slot.clear();
    }

    @Override
    public void setChanged() {
        this.slot.onChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }
}
