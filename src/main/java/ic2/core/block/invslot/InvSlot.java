/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.block.invslot;

import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InvSlot
implements Iterable<ItemStack> {
    public final IInventorySlotHolder<?> base;
    public final String name;
    private final ItemStack[] contents;
    protected final Access access;
    public final InvSide preferredSide;
    private int stackSizeLimit;

    public InvSlot(IInventorySlotHolder<?> iInventorySlotHolder, String string, Access access, int n) {
        this(iInventorySlotHolder, string, access, n, InvSide.ANY);
    }

    public InvSlot(IInventorySlotHolder<?> iInventorySlotHolder, String string, Access access, int n, InvSide invSide) {
        if (n <= 0) {
            throw new IllegalArgumentException("invalid slot count: " + n);
        }
        this.contents = new ItemStack[n];
        this.clear();
        this.base = iInventorySlotHolder;
        this.name = string;
        this.access = access;
        this.preferredSide = invSide;
        this.stackSizeLimit = 64;
        iInventorySlotHolder.addInventorySlot(this);
    }

    public InvSlot(int n) {
        this.contents = new ItemStack[n];
        this.clear();
        this.base = null;
        this.name = null;
        this.access = Access.NONE;
        this.preferredSide = InvSide.ANY;
    }

    public void readFromNbt(CompoundTag compoundTag) {
        this.clear();
        ListTag listTag = compoundTag.getList("Contents", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            int n = compoundTag2.getByte("Index") & 0xFF;
            if (n >= this.size()) {
                IC2.log.error(LogCategory.Block, "Can't load item stack for %s, slot %s, index %d is out of bounds.", Util.toString(this.base.getParent()), this.name, n);
                continue;
            }
            ItemStack itemStack = ItemStack.parseOptional(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), compoundTag2);
            if (StackUtil.isEmpty(itemStack)) {
                IC2.log.warn(LogCategory.Block, "Can't load item stack %s for %s, slot %s, index %d, no matching item for %d:%d.", StackUtil.toStringSafe(itemStack), Util.toString(this.base.getParent()), this.name, n, compoundTag2.getShort("id"), compoundTag2.getShort("Damage"));
                continue;
            }
            if (!this.isEmpty(n)) {
                IC2.log.error(LogCategory.Block, "Loading content to non-empty slot for %s, slot %s, index %d, replacing %s with %s.", Util.toString(this.base.getParent()), this.name, n, this.get(n), itemStack);
            }
            this.putFromNBT(n, itemStack);
        }
        this.onChanged();
    }

    public void writeToNbt(CompoundTag compoundTag) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.contents.length; ++i) {
            ItemStack itemStack = this.contents[i];
            if (StackUtil.isEmpty(itemStack)) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.putByte("Index", (byte)i);
            itemStack.save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), compoundTag2);
            listTag.add(compoundTag2);
        }
        compoundTag.put("Contents", (Tag)listTag);
    }

    public int size() {
        return this.contents.length;
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.contents) {
            if (StackUtil.isEmpty(itemStack)) continue;
            return false;
        }
        return true;
    }

    public boolean isEmpty(int n) {
        return StackUtil.isEmpty(this.contents[n]);
    }

    public ItemStack get() {
        return this.get(0);
    }

    public ItemStack get(int n) {
        return this.contents[n];
    }

    public void put(ItemStack itemStack) {
        this.put(0, itemStack);
    }

    protected void putFromNBT(int n, ItemStack itemStack) {
        this.contents[n] = itemStack;
    }

    public void put(int n, ItemStack itemStack) {
        if (StackUtil.isEmpty(itemStack)) {
            itemStack = StackUtil.emptyStack;
        }
        this.contents[n] = itemStack;
        this.onChanged();
    }

    public void clear() {
        Arrays.fill(this.contents, StackUtil.emptyStack);
    }

    public void clear(int n) {
        this.put(n, StackUtil.emptyStack);
    }

    public void onChanged() {
    }

    public boolean accepts(ItemStack itemStack) {
        return true;
    }

    public boolean canInput() {
        return this.access == Access.I || this.access == Access.IO;
    }

    public boolean canOutput() {
        return this.access == Access.O || this.access == Access.IO;
    }

    public void organize() {
        block0: for (int i = 0; i < this.contents.length - 1; ++i) {
            ItemStack itemStack = this.contents[i];
            if (!StackUtil.isEmpty(itemStack) && StackUtil.getSize(itemStack) >= itemStack.getMaxStackSize()) continue;
            for (int j = i + 1; j < this.contents.length; ++j) {
                ItemStack itemStack2 = this.contents[j];
                if (StackUtil.isEmpty(itemStack2)) continue;
                if (StackUtil.isEmpty(itemStack)) {
                    this.contents[j] = StackUtil.emptyStack;
                    this.contents[i] = itemStack = itemStack2;
                    continue;
                }
                if (!StackUtil.checkItemEqualityStrict(itemStack, itemStack2)) continue;
                int n = Math.min(this.getStackSizeLimit(), itemStack.getMaxStackSize() - StackUtil.getSize(itemStack));
                int n2 = StackUtil.getSize(itemStack2);
                if (n2 <= n) {
                    this.contents[j] = StackUtil.emptyStack;
                    this.contents[i] = itemStack = StackUtil.incSize(itemStack, n2);
                    if (n2 != n) continue;
                    continue block0;
                }
                this.contents[j] = StackUtil.decSize(itemStack2, n);
                this.contents[i] = StackUtil.incSize(itemStack, n);
                continue block0;
            }
        }
    }

    public int getStackSizeLimit() {
        return this.stackSizeLimit;
    }

    public void setStackSizeLimit(int n) {
        this.stackSizeLimit = n;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new Iterator<ItemStack>(){
            private int idx = 0;

            @Override
            public boolean hasNext() {
                return this.idx < InvSlot.this.contents.length;
            }

            @Override
            public ItemStack next() {
                if (this.idx >= InvSlot.this.contents.length) {
                    throw new NoSuchElementException();
                }
                return InvSlot.this.contents[this.idx++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String toString() {
        String string = this.name + "[" + this.contents.length + "]: ";
        for (int i = 0; i < this.contents.length; ++i) {
            string = string + this.contents[i];
            if (i >= this.contents.length - 1) continue;
            string = string + ", ";
        }
        return string;
    }

    protected ItemStack[] backup() {
        ItemStack[] itemStackArray = new ItemStack[this.contents.length];
        for (int i = 0; i < this.contents.length; ++i) {
            ItemStack itemStack = this.contents[i];
            itemStackArray[i] = StackUtil.isEmpty(itemStack) ? StackUtil.emptyStack : itemStack.copy();
        }
        return itemStackArray;
    }

    protected void restore(ItemStack[] itemStackArray) {
        if (itemStackArray.length != this.contents.length) {
            throw new IllegalArgumentException("invalid array size");
        }
        for (int i = 0; i < this.contents.length; ++i) {
            this.contents[i] = itemStackArray[i];
        }
    }

    public void onPickupFromSlot(Player player, ItemStack itemStack) {
    }

    public static enum InvSide {
        ANY(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
        TOP(Direction.UP),
        BOTTOM(Direction.DOWN),
        SIDE(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST),
        NOTSIDE(new Direction[0]);

        private Set<Direction> acceptedSides;

        private InvSide(Direction ... directionArray) {
            if (directionArray.length == 0) {
                this.acceptedSides = Collections.emptySet();
            } else {
                EnumSet<Direction> enumSet = EnumSet.noneOf(Direction.class);
                enumSet.addAll(Arrays.asList(directionArray));
                this.acceptedSides = Collections.unmodifiableSet(enumSet);
            }
        }

        public boolean matches(Direction direction) {
            return this.acceptedSides.contains(direction);
        }

        public Set<Direction> getAcceptedSides() {
            return this.acceptedSides;
        }
    }

    public static enum Access {
        NONE,
        I,
        O,
        IO;


        public boolean isInput() {
            return (this.ordinal() & 1) != 0;
        }

        public boolean isOutput() {
            return (this.ordinal() & 2) != 0;
        }
    }
}

