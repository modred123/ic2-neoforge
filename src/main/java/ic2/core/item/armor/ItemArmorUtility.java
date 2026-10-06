/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.Item$Properties
 */
package ic2.core.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class ItemArmorUtility
extends ArmorItem {
    public ItemArmorUtility(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Item.Properties properties) {
        // 1.21.1 修复（第二十四轮）：强制装甲不可堆叠（maxStackSize = 1），理由同 ItemArmorIC2。
        // 本类覆盖另一支装甲：防化服、CF 背包（ItemArmorFluidTank）、喷气背包、
        // 夜视仪（另有 durability(27) 已隐含 stacksTo(1)，此处重复设置无副作用）、
        // 经典 CF 背包、太阳能头盔、静电靴。
        super(armorMaterial, type, properties.stacksTo(1));
    }

    /** ex112 兼容构造：ItemName + 字符串类型 + EquipmentSlot */
    protected ItemArmorUtility(ic2.core.ref.ItemName name, String materialName, net.minecraft.world.entity.EquipmentSlot slot) {
        this(ItemArmorElectric.resolveMaterial(name, materialName), ItemArmorElectric.resolveType(slot), new Item.Properties());
        ic2.core.init.BlocksItems.registerItem(this, ic2.core.IC2.getIdentifier(name.name()));
        name.setInstance(this);
    }
}

