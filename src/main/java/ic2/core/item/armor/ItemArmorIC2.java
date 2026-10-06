/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item.armor;

import ic2.api.item.IMetalArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemArmorIC2
extends ArmorItem
implements IMetalArmor {
    public ItemArmorIC2(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Item.Properties properties) {
        // 1.21.1 修复（第二十四轮）：强制装甲不可堆叠（maxStackSize = 1）。
        //
        // 1.12.2 的 vanilla ItemArmor 构造器内置 this.maxStackSize = 1；
        // 1.21.1 的 ArmorItem 构造器已把该逻辑移出（字节码实证：仅设置 material/type/
        // dispenseBehavior/defaultModifiers，不再触碰堆叠上限），改由 vanilla 在 Items.java
        // 注册时用 new Item.Properties().durability(Type.getDurability(n)) 表达 —— 而
        // Item.Properties.durability(int) 内部会把 MAX_STACK_SIZE 一并置为 1。
        //
        // IC2 注册装甲时传的是裸 new Item.Properties()（既无 durability，也无 stacksTo），
        // 于是堆叠上限退回默认值 64，与 vanilla/1.12.2 行为完全相反。
        // 对电装甲（ItemArmorElectric 子树）更是直接触发「可堆叠 ↔ charge() 拒绝 count>1」
        // 的同源缺陷：一叠纳米/量子装甲既放不进充电槽，也不显示电量 tooltip。
        // 此处收口（本类覆盖全部电装甲分支），恢复不可堆叠语义。
        super(armorMaterial, type, properties.stacksTo(1));
    }

    @Override
    public boolean isMetalArmor(ItemStack itemStack, Player player) {
        return true;
    }
}

