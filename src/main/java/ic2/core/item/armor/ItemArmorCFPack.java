/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package ic2.core.item.armor;

import ic2.core.item.armor.ItemArmorFluidTank;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.ref.Ic2Fluids;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemArmorCFPack
extends ItemArmorFluidTank {
    public ItemArmorCFPack(Item.Properties properties) {
        super(net.minecraft.core.Holder.direct(Ic2ArmorMaterials.CF_PACK), properties, ArmorItem.Type.CHESTPLATE, Ic2Fluids.CONSTRUCTION_FOAM.still, 80000);
    }

    // TODO: 1.21.1 CreativeModeTab 重构，fillItemCategory 已移除
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        ItemStack itemStack = new ItemStack(this);
        this.filltank(itemStack);
        nonNullList.add(itemStack);
        nonNullList.add(new ItemStack(this));
    }
}

