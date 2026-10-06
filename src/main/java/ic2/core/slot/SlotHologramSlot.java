/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.slot;

import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotHologramSlot
extends Slot {
    protected final ItemStack[] stacks;
    protected final int index;
    protected final int stackSizeLimit;
    protected final ChangeCallback changeCallback;

    public SlotHologramSlot(ItemStack[] itemStackArray, int n, int n2, int n3, int n4, ChangeCallback changeCallback) {
        super((Container)new DummyInventory(), 0, n2, n3);
        if (n >= itemStackArray.length) {
            throw new ArrayIndexOutOfBoundsException(n);
        }
        this.stacks = itemStackArray;
        this.index = n;
        this.stackSizeLimit = n4;
        this.changeCallback = changeCallback;
    }

    public boolean mayPickup(Player player) {
        return false;
    }

    public int getMaxStackSize() {
        return this.stackSizeLimit;
    }

    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    public ItemStack getItem() {
        return StackUtil.wrapEmpty(this.stacks[this.index]);
    }

    public void set(ItemStack itemStack) {
        this.stacks[this.index] = itemStack;
    }

    public void setChanged() {
        if (Util.inDev()) {
            System.out.println(StackUtil.toStringSafe(this.stacks));
        }
        if (this.changeCallback != null) {
            this.changeCallback.onChanged(this.index);
        }
    }

    public ItemStack remove(int n) {
        return StackUtil.emptyStack;
    }

    public ItemStack slotClick(int n, ClickType clickType, Player player, AbstractContainerMenu abstractContainerMenu) {
        if (Util.inDev() && player.getCommandSenderWorld().isClientSide) {
            System.out.printf("button=%d clickType=%s stack=%s%n", n, clickType, abstractContainerMenu.getCarried());
        }
        if (clickType == ClickType.PICKUP && (n == 0 || n == 1)) {
            ItemStack itemStack = abstractContainerMenu.getCarried();
            ItemStack itemStack2 = this.stacks[this.index];
            if (!StackUtil.isEmpty(itemStack)) {
                int n2;
                int n3;
                int n4 = StackUtil.getSize(itemStack2);
                if (n4 + (n3 = n == 0 ? StackUtil.getSize(itemStack) : 1) > (n2 = Math.min(itemStack.getMaxStackSize(), this.stackSizeLimit))) {
                    n3 = Math.max(0, n2 - n4);
                }
                if (n4 == 0) {
                    this.stacks[this.index] = StackUtil.copyWithSize(itemStack, n3);
                } else if (StackUtil.checkItemEquality(itemStack, itemStack2)) {
                    if (Util.inDev()) {
                        System.out.println("add " + n3 + " to " + itemStack2 + " -> " + (n4 + n3));
                    }
                    this.stacks[this.index] = StackUtil.incSize(itemStack2, n3);
                } else {
                    this.stacks[this.index] = StackUtil.copyWithSize(itemStack, Math.min(StackUtil.getSize(itemStack), n2));
                }
            } else if (!StackUtil.isEmpty(itemStack2)) {
                int n5;
                this.stacks[this.index] = n == 0 ? StackUtil.emptyStack : ((n5 = StackUtil.getSize(itemStack2) / 2) <= 0 ? StackUtil.emptyStack : StackUtil.setSize(itemStack2, n5));
            }
            this.setChanged();
        }
        return StackUtil.emptyStack;
    }

    private static final class DummyInventory
    implements Container {
        private DummyInventory() {
        }

        public int getContainerSize() {
            return 1;
        }

        public boolean isEmpty() {
            return false;
        }

        public ItemStack getItem(int n) {
            return StackUtil.emptyStack;
        }

        public ItemStack removeItem(int n, int n2) {
            return StackUtil.emptyStack;
        }

        public ItemStack removeItemNoUpdate(int n) {
            return StackUtil.emptyStack;
        }

        public void setItem(int n, ItemStack itemStack) {
        }

        public void setChanged() {
        }

        public boolean stillValid(Player player) {
            return true;
        }

        public void clearContent() {
        }
    }

    public static interface ChangeCallback {
        public void onChanged(int var1);
    }
}

