/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.DyeableLeatherItem
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 */
package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IHazmatLike;
import ic2.api.item.IItemHudProvider;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemArmorQuantumSuit
extends ItemArmorElectric
implements IJetpack,
IHazmatLike,
IItemHudProvider {
    public static final int[] CHARGED_PROTECTION = new int[]{3, 6, 8, 3};
    private static final int defaultColor = -1;

    public ItemArmorQuantumSuit(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Item.Properties properties) {
        super(armorMaterial, type, properties, 1.0E7, 12000.0, 4);
    }

    @Override
    public int getEnergyPerDamage() {
        return 20000;
    }

    public boolean hasCustomColor(ItemStack itemStack) {
        return this.getColor(itemStack) != -1;
    }

    public void clearColor(ItemStack itemStack) {
        CompoundTag compoundTag = this.getDisplayNbt(itemStack, false);
        if (compoundTag == null || !compoundTag.contains("color", 3)) {
            return;
        }
        compoundTag.remove("color");
        if (compoundTag.isEmpty()) {
            // 1.21.1 修复（第二十七轮）：原 StackUtil.getTag(itemStack).remove(...) 改的是副本，删除无效。
            StackUtil.getOrCreateNbtData(itemStack).remove("display");
        }
    }

    public int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = this.getDisplayNbt(itemStack, false);
        if (compoundTag == null || !compoundTag.contains("color", 3)) {
            return -1;
        }
        return compoundTag.getInt("color");
    }

    public void setColor(ItemStack itemStack, int n) {
        CompoundTag compoundTag = this.getDisplayNbt(itemStack, true);
        assert (compoundTag != null);
        compoundTag.putInt("color", n);
    }

    private CompoundTag getDisplayNbt(ItemStack itemStack, boolean bl) {
        // 1.21.1 修复（第二十七轮）：原实现全程操作 StackUtil.getTag() 的【副本】——
        // 无论是 setTag(...) 分支还是 put("display", ...) 分支，返回的都是"孤儿"子 tag，
        // 调用方对其 putXxx/remove 全部丢失 ⇒ 量子套颜色（setColor/getColor/hasCustomColor/clearColor）整体失效。
        // 改为基于活引用；同时保留原语义：bl=false 且本就没有 NBT 时不创建。
        if (itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) == null && !bl) {
            return null;
        }
        CompoundTag compoundTag;
        CompoundTag compoundTag2 = StackUtil.getOrCreateNbtData(itemStack);
        if (!compoundTag2.contains("display", 10)) {
            if (!bl) {
                return null;
            }
            compoundTag = new CompoundTag();
            compoundTag2.put("display", (Tag)compoundTag);
        } else {
            compoundTag = compoundTag2.getCompound("display");
        }
        return compoundTag;
    }

    @Override
    public boolean addsProtection(LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        return ElectricItem.manager.getCharge(itemStack) > 0.0;
    }

    public boolean absorbFall(ItemStack itemStack, LivingEntity livingEntity, float f) {
        int n = Math.max((int)f - 10, 0);
        double d = this.getEnergyPerDamage() * n;
        if (d > ElectricItem.manager.getCharge(itemStack)) {
            return false;
        }
        ElectricItem.manager.discharge(itemStack, d, Integer.MAX_VALUE, true, false, false);
        return true;
    }

    public Rarity getRarity(ItemStack itemStack) {
        return Rarity.RARE;
    }

    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean drainEnergy(ItemStack itemStack, int n) {
        return ElectricItem.manager.discharge(itemStack, n + 6, Integer.MAX_VALUE, true, false, false) > 0.0;
    }

    @Override
    public float getPower(ItemStack itemStack) {
        return 1.0f;
    }

    @Override
    public float getDropPercentage(ItemStack itemStack) {
        return 0.05f;
    }

    @Override
    public double getChargeLevel(ItemStack itemStack) {
        return ElectricItem.manager.getCharge(itemStack) / this.getMaxCharge(itemStack);
    }

    @Override
    public boolean isJetpackActive(ItemStack itemStack) {
        return true;
    }

    @Override
    public float getHoverMultiplier(ItemStack itemStack, boolean bl) {
        return 0.1f;
    }

    @Override
    public float getWorldHeightDivisor(ItemStack itemStack) {
        return 0.9f;
    }

    @Override
    public boolean doesProvideHUD(ItemStack itemStack) {
        return this.getEquipmentSlot() == EquipmentSlot.HEAD && ElectricItem.manager.getCharge(itemStack) > 0.0;
    }

    @Override
    public HudMode getHudMode(ItemStack itemStack) {
        return HudMode.getFromID(StackUtil.getOrCreateNbtData(itemStack).getShort("HudMode"));
    }
}

