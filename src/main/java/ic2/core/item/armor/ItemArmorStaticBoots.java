/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ItemArmorStaticBoots
extends ItemArmorUtility {
    public ItemArmorStaticBoots() {
        super(ItemName.static_boots, "rubber", EquipmentSlot.FEET);
    }

    public void onArmorTick(Level world, Player player, ItemStack stack) {
        double distance;
        boolean isNotWalking;
        if (StackUtil.isEmpty((ItemStack)player.getInventory().armor.get(2))) {
            return;
        }
        boolean ret = false;
        CompoundTag compound = StackUtil.getOrCreateNbtData(stack);
        boolean bl = isNotWalking = player.getVehicle() != null || player.isInWater();
        if (!compound.contains("x") || isNotWalking) {
            compound.putInt("x", (int)player.getX());
        }
        if (!compound.contains("z") || isNotWalking) {
            compound.putInt("z", (int)player.getZ());
        }
        if ((distance = Math.sqrt((compound.getInt("x") - (int)player.getX()) * (compound.getInt("x") - (int)player.getX()) + (compound.getInt("z") - (int)player.getZ()) * (compound.getInt("z") - (int)player.getZ()))) >= 5.0) {
            compound.putInt("x", (int)player.getX());
            compound.putInt("z", (int)player.getZ());
            boolean bl2 = ret = ElectricItem.manager.charge((ItemStack)player.getInventory().armor.get(2), Math.min(3.0, distance / 5.0), Integer.MAX_VALUE, true, false) > 0.0;
        }
        if (ret) {
            player.containerMenu.broadcastChanges();
        }
    }
}

