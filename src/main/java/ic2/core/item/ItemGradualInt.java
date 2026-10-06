/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.core.NonNullList
 */
package ic2.core.item;

import ic2.api.item.ICustomDamageItem;
import ic2.core.IC2;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;

public class ItemGradualInt
extends ItemIC2
implements ICustomDamageItem {
    private static final boolean alwaysShowDurability = true;
    private static final String nbtKey = "advDmg";
    private final int maxDamage;

    public ItemGradualInt(ItemName name, int maxDamage) {
        super(name);
        this.setNoRepair(true);
        this.maxDamage = maxDamage;
    }

    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        return (double)this.getCustomDamage(stack) / (double)this.getMaxCustomDamage(stack);
    }

    @Override
    public boolean isDamageable(ItemStack itemStack) {
        return true;
    }

    public boolean isDamaged(ItemStack stack) {
        return this.getCustomDamage(stack) > 0;
    }

    public int getDamage(ItemStack stack) {
        return this.getCustomDamage(stack);
    }

    @Override
    public int getCustomDamage(ItemStack stack) {
        if (StackUtil.getTag(stack) == null) {
            return 0;
        }
        return StackUtil.getTag(stack).getInt(nbtKey);
    }

    public int getMaxDamage(ItemStack stack) {
        return this.getMaxCustomDamage(stack);
    }

    @Override
    public int getMaxCustomDamage(ItemStack stack) {
        return this.maxDamage;
    }

    public void setDamage(ItemStack stack, int damage) {
        int prev = this.getCustomDamage(stack);
        if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid gradual item damage application (%d):", damage - prev);
        }
    }

    @Override
    public void setCustomDamage(ItemStack stack, int damage) {
        CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
        nbt.putInt(nbtKey, damage);
    }

    @Override
    public boolean applyCustomDamage(ItemStack stack, int damage, LivingEntity src) {
        this.setCustomDamage(stack, this.getCustomDamage(stack) + damage);
        return true;
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
        if (!this.allowedIn(tab)) {
            return;
        }
        ItemStack stack = new ItemStack((Item)this);
        this.setCustomDamage(stack, 0);
        subItems.add(stack);
    }
}

