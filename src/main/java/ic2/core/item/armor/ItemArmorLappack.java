/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.item.Rarity
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.armor;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.profile.NotExperimental;
import ic2.core.ref.ItemName;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;

@NotExperimental
public class ItemArmorLappack
extends ItemArmorElectric {
    public ItemArmorLappack() {
        super(ItemName.lappack, "lappack", net.minecraft.world.entity.EquipmentSlot.CHEST, 2.0E7, 2500.0, 4);
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

    @OnlyIn(Dist.CLIENT)
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}

