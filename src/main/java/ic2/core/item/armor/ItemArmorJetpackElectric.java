/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraftforge.common.ISpecialArmor$ArmorProperties
 */
package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.ItemName;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorJetpackElectric
extends ItemArmorElectric
implements IJetpack {
    public ItemArmorJetpackElectric() {
        super(ItemName.jetpack_electric, "jetpack", net.minecraft.world.entity.EquipmentSlot.CHEST, 30000.0, 60.0, 1);

    }

    @Override
    public boolean drainEnergy(ItemStack pack, int amount) {
        return ElectricItem.manager.discharge(pack, amount + 6, Integer.MAX_VALUE, true, false, false) > 0.0;
    }

    @Override
    public float getPower(ItemStack stack) {
        return 0.7f;
    }

    @Override
    public float getDropPercentage(ItemStack stack) {
        return 0.05f;
    }

    @Override
    public boolean isJetpackActive(ItemStack stack) {
        return true;
    }

    @Override
    public double getChargeLevel(ItemStack stack) {
        return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
    }

    @Override
    public float getHoverMultiplier(ItemStack stack, boolean upwards) {
        return 0.1f;
    }

    @Override
    public float getWorldHeightDivisor(ItemStack stack) {
        return 1.28f;
    }

        public int getEnergyPerDamage() {
        return 0;
    }

        public double getDamageAbsorptionRatio() {
        return 0.0;
    }
}

