/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item.armor;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.ref.ItemName;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorBatpack
extends ItemArmorElectric {
    public ItemArmorBatpack() {
        super(ItemName.batpack, "batpack", net.minecraft.world.entity.EquipmentSlot.CHEST, 60000.0, 100.0, 1);
    }

    @Override
    public boolean canProvideEnergy(ItemStack stack) {
        return true;
    }

        public double getDamageAbsorptionRatio() {
        return 0.0;
    }

        public int getEnergyPerDamage() {
        return 0;
    }
}

