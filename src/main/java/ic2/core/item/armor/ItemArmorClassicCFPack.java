/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item.armor;

import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.ItemName;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorClassicCFPack
extends ItemArmorUtility {
    public ItemArmorClassicCFPack() {
        super(ItemName.cf_pack, "batpack", net.minecraft.world.entity.EquipmentSlot.CHEST);

    }

    public boolean getCFPellet(Player player, ItemStack pack) {
        if (pack.getDamageValue() > 0) {
            pack.setDamageValue(pack.getDamageValue() - 1);
            return true;
        }
        return false;
    }

    public boolean isBarVisible(ItemStack stack) {
        return true;
    }
}

