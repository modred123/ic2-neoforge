/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.EquipmentSlot$Type
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package ic2.api.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IHazmatLike {
    public boolean addsProtection(LivingEntity var1, EquipmentSlot var2, ItemStack var3);

    default public boolean fullyProtects(LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        return false;
    }

    public static boolean hasCompleteHazmat(LivingEntity livingEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
            if (itemStack == null || !(itemStack.getItem() instanceof IHazmatLike)) {
                return false;
            }
            IHazmatLike iHazmatLike = (IHazmatLike)itemStack.getItem();
            if (!iHazmatLike.addsProtection(livingEntity, equipmentSlot, itemStack)) {
                return false;
            }
            if (!iHazmatLike.fullyProtects(livingEntity, equipmentSlot, itemStack)) continue;
            return true;
        }
        return true;
    }
}

