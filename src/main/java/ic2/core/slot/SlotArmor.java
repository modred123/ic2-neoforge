/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SlotArmor
extends Slot {
    // 1.21.1: InventoryMenu 不再有 ResourceLocation[] EMPTY_ARMOR_SLOT_TEXTURES 字段（已内联为单独常量），
    // 原 1.19.2 用反射按类型获取，现改为直接用公开常量构建，顺序对应 EquipmentSlot.getIndex(): 0=HEAD 1=CHEST 2=LEGS 3=FEET
    private static final ResourceLocation[] EMPTY_ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
        InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
        InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
        InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
        InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
    };
    private final EquipmentSlot armorType;

    public SlotArmor(Inventory inventory, EquipmentSlot equipmentSlot, int n, int n2) {
        super((Container)inventory, 36 + equipmentSlot.getIndex(), n, n2);
        this.armorType = equipmentSlot;
    }

    public boolean mayPlace(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == null) {
            return false;
        }
        return itemStack.getEquipmentSlot() == this.armorType;
    }

    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        int n = this.armorType.getIndex();
        if (n < 0 || n >= EMPTY_ARMOR_SLOT_TEXTURES.length) {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
        }
        return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_TEXTURES[n]);
    }
}

