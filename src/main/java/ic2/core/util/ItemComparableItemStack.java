/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package ic2.core.util;

import ic2.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemComparableItemStack {
    private final Item item;
    private final CompoundTag nbt;
    private final int hashCode;

    public ItemComparableItemStack(ItemStack itemStack, boolean bl) {
        this.item = itemStack.getItem();
        net.minecraft.world.item.component.CustomData customData = itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        CompoundTag compoundTag = customData == null ? null : customData.copyTag();
        if (compoundTag != null) {
            if (compoundTag.isEmpty()) {
                compoundTag = null;
            } else {
                if (bl) {
                    compoundTag = compoundTag.copy();
                }
                boolean bl2 = bl;
                for (String string : StackUtil.ignoredNbtKeys) {
                    if (!bl2 && compoundTag.contains(string)) {
                        compoundTag = compoundTag.copy();
                        bl2 = true;
                    }
                    compoundTag.remove(string);
                }
                if (compoundTag.isEmpty()) {
                    compoundTag = null;
                }
            }
        }
        this.nbt = compoundTag;
        this.hashCode = this.calculateHashCode();
    }

    private ItemComparableItemStack(ItemComparableItemStack itemComparableItemStack) {
        this.item = itemComparableItemStack.item;
        this.nbt = itemComparableItemStack.nbt != null ? itemComparableItemStack.nbt.copy() : null;
        this.hashCode = itemComparableItemStack.hashCode;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ItemComparableItemStack)) {
            return false;
        }
        ItemComparableItemStack itemComparableItemStack = (ItemComparableItemStack)object;
        if (itemComparableItemStack.hashCode != this.hashCode) {
            return false;
        }
        if (itemComparableItemStack == this) {
            return true;
        }
        return itemComparableItemStack.item == this.item && (itemComparableItemStack.nbt == null && this.nbt == null || itemComparableItemStack.nbt != null && this.nbt != null && itemComparableItemStack.nbt.equals((Object)this.nbt));
    }

    public int hashCode() {
        return this.hashCode;
    }

    private int calculateHashCode() {
        int n = 0;
        if (this.item != null) {
            n = System.identityHashCode(this.item);
        }
        if (this.nbt != null) {
            n = n * 31 + this.nbt.hashCode();
        }
        return n;
    }

    public ItemComparableItemStack copy() {
        if (this.nbt == null) {
            return this;
        }
        return new ItemComparableItemStack(this);
    }

    public ItemStack toStack() {
        return this.toStack(1);
    }

    public ItemStack toStack(int n) {
        if (this.item == null) {
            return null;
        }
        ItemStack itemStack = new ItemStack((ItemLike)this.item, n);
        // 第三十六轮修复：1.12.2 这里是 `ret.setTagCompound(this.nbt)`，而 `setTagCompound(null)` 合法（= 清空 tag）。
        // 1.21 的 `CustomData.of(null)` 会直接 NPE（CustomData.java:52 `CompoundTag.copy()` 收到 null）——
        // 于是"扫到任何一个没有自定义 NBT 的矿物"（矿石方块物品基本都没有）都会在这一步崩，
        // 异常被服务端 suppress（logs/2026-10-02-2.log:995，路径 ItemScanner.scanMapToSortedList → toStack），
        // 表现依旧是"扫描结果为空"。语义修正：nbt 为 null 时不设置组件（等价 1.12.2 的清空）。
        if (this.nbt != null) {
            itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(this.nbt));
        }
        return itemStack;
    }
}

