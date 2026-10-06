/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.EquipmentSlot$Type
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item.armor;

import ic2.api.item.IHazmatLike;
import ic2.core.Ic2DamageSource;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.Ic2ArmorMaterials;
import java.util.function.Consumer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemArmorHazmat
extends ItemArmorUtility
implements IHazmatLike {
    public ItemArmorHazmat(ArmorItem.Type type, Item.Properties properties) {
        // 1.21.1 修复（第二十六轮）：补回 64 点真实耐久。
        //
        // 1.12.2 的 ItemArmorHazmat 构造器有 this.setMaxDamage(64)，防化服与橡胶靴都会磨损
        // （它们既不耗电也没有其它替代资源机制，不属于"永不损坏"范畴）。
        // 1.19.2 / 迁移版漏掉了这一句，除"永不损坏"外还连带打坏了两处依赖 getMaxDamage() 的逻辑：
        //   · ItemArmorHazmat.absorbFall() —— `n2 > getMaxDamage() - getDamageValue()` 在
        //     MAX_DAMAGE 缺失（=0）时恒为真，直接 return false ⇒ **橡胶靴的摔落伤害吸收永久失效**；
        //   · 1.12.2 的 getProperties() 用 `(maxDamage - damage + 2) * 2 * 25` 计算可吸收量，
        //     同样以 maxDamage 为基准。
        // 故此处恢复 setMaxDamage(64) 的语义（1.12.2 对全部 4 件同类装甲统一给 64）。
        super(net.minecraft.core.Holder.direct(Ic2ArmorMaterials.HAZMAT), type, properties.durability(64));
    }

    @Override
    public boolean addsProtection(LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        return true;
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

    public static boolean hazmatAbsorbs(DamageSource damageSource) {
        return damageSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) || damageSource.is(Ic2DamageSource.ELECTRICITY) || damageSource.is(Ic2DamageSource.RADIATION);
    }

    public boolean absorbFall(ItemStack itemStack, LivingEntity livingEntity, float f) {
        int n = Math.max((int)f - 3, 0);
        if (n >= 8) {
            return false;
        }
        int n2 = (n + 1) / 2;
        if (n2 <= 0 || n2 > itemStack.getMaxDamage() - itemStack.getDamageValue()) {
            return false;
        }
        itemStack.hurtAndBreak(n2, livingEntity, this.getEquipmentSlot());
        return true;
    }

}

