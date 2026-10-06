/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.ItemName;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemArmorSolarHelmet
extends ItemArmorUtility {
    public ItemArmorSolarHelmet() {
        super(ItemName.solar_helmet, "solar", net.minecraft.world.entity.EquipmentSlot.HEAD);

    }

    public void onArmorTick(Level world, Player player, ItemStack stack) {
        double chargeAmount;
        boolean ret = false;
        if (player.getInventory().armor.get(2) != null && (chargeAmount = (double)TileEntitySolarGenerator.getSkyLight(player.level(), player.blockPosition())) > 0.0) {
            boolean bl = ret = ElectricItem.manager.charge((ItemStack)player.getInventory().armor.get(2), chargeAmount, Integer.MAX_VALUE, true, false) > 0.0;
        }
        if (ret) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }
}

