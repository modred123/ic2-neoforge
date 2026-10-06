/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item;

import ic2.core.IC2;
import ic2.core.item.IPseudoDamageItem;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DamageHandler {
    public static int getDamage(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == null) {
            return 0;
        }
        return itemStack.getDamageValue();
    }

    public static void setDamage(ItemStack itemStack, int n, boolean bl) {
        Item item = itemStack.getItem();
        if (item == null) {
            return;
        }
        if (item instanceof IPseudoDamageItem) {
            if (!bl) {
                throw new IllegalStateException("can't damage " + itemStack + " physically");
            }
            ((IPseudoDamageItem)item).setStackDamage(itemStack, n);
        } else if (itemStack.isDamageableItem()) {
            itemStack.setDamageValue(n);
        }
    }

    public static int getMaxDamage(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == null) {
            return 0;
        }
        return itemStack.getMaxDamage();
    }

    public static boolean damage(ItemStack itemStack, int n, LivingEntity livingEntity, InteractionHand interactionHand) {
        Item item = itemStack.getItem();
        if (item == null) {
            return false;
        }
        if (livingEntity != null) {
            EquipmentSlot slot = interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            itemStack.hurtAndBreak(n, livingEntity, slot);
            return true;
        }
        itemStack.setDamageValue(itemStack.getDamageValue() + n);
        return itemStack.getDamageValue() >= itemStack.getMaxDamage();
    }
}

