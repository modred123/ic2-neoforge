package ic2.core.block;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public abstract class SimpleCraftingInventory
implements CraftingContainer {
    private final int width;
    private final int height;
    private final int size;

    public SimpleCraftingInventory(int n, int n2) {
        this.width = n;
        this.height = n2;
        this.size = n * n2;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public ItemStack getItem(int n) {
        if (n >= this.size) {
            return StackUtil.emptyStack;
        }
        return StackUtil.wrapEmpty(this.get(n));
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        this.set(n, itemStack);
    }

    @Override
    public ItemStack removeItemNoUpdate(int n) {
        ItemStack itemStack;
        if (n >= this.size || StackUtil.isEmpty(itemStack = this.get(n))) {
            return StackUtil.emptyStack;
        }
        this.set(n, StackUtil.emptyStack);
        return itemStack;
    }

    @Override
    public ItemStack removeItem(int n, int n2) {
        ItemStack itemStack;
        if (n >= this.size || n2 <= 0 || StackUtil.isEmpty(itemStack = this.get(n))) {
            return StackUtil.emptyStack;
        }
        return itemStack.split(n2);
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.size; ++i) {
            if (StackUtil.isEmpty(this.get(i))) continue;
            return false;
        }
        return true;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.size; ++i) {
            this.set(i, StackUtil.emptyStack);
        }
    }

    @Override
    public List<ItemStack> getItems() {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (int i = 0; i < this.size; ++i) {
            arrayList.add(this.get(i));
        }
        return arrayList;
    }

    public void fillStackedContents(StackedContents stackedContents) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack itemStack = this.get(i);
            if (StackUtil.isEmpty(itemStack)) continue;
            stackedContents.accountSimpleStack(itemStack);
        }
    }

    protected abstract ItemStack get(int var1);

    protected abstract void set(int var1, ItemStack var2);

    public static class InvSlotCraftingInventory
    extends SimpleCraftingInventory {
        private final InvSlot invSlot;

        public InvSlotCraftingInventory(InvSlot invSlot, int n) {
            super(n, (invSlot.size() + n - 1) / n);
            this.invSlot = invSlot;
        }

        @Override
        protected ItemStack get(int n) {
            return this.invSlot.get(n);
        }

        @Override
        protected void set(int n, ItemStack itemStack) {
            this.invSlot.put(n, itemStack);
        }
    }

    public static class ArrayCraftingInventory
    extends SimpleCraftingInventory {
        private final ItemStack[] items;

        public ArrayCraftingInventory(ItemStack[] itemStackArray, int n) {
            super(n, (itemStackArray.length + n - 1) / n);
            this.items = itemStackArray;
        }

        @Override
        protected ItemStack get(int n) {
            return this.items[n];
        }

        @Override
        protected void set(int n, ItemStack itemStack) {
            this.items[n] = itemStack;
        }
    }
}
