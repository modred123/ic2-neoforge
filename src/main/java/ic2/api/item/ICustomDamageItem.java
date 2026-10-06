/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package ic2.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ICustomDamageItem {
    public int getCustomDamage(ItemStack var1);

    public int getMaxCustomDamage(ItemStack var1);

    public void setCustomDamage(ItemStack var1, int var2);

    public boolean applyCustomDamage(ItemStack var1, int var2, LivingEntity var3);
}

