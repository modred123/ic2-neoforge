/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ArrayListMultimap
 *  com.google.common.collect.Multimap
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package ic2.api.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import ic2.api.item.IBoxable;
import ic2.api.item.IMetalArmor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemWrapper {
    private static final Multimap<Item, IBoxable> boxableItems = ArrayListMultimap.create();
    private static final Multimap<Item, IMetalArmor> metalArmorItems = ArrayListMultimap.create();

    public static void registerBoxable(Item item, IBoxable iBoxable) {
        boxableItems.put(item, iBoxable);
    }

    public static boolean canBeStoredInToolbox(ItemStack itemStack) {
        Item item = itemStack.getItem();
        for (IBoxable iBoxable : boxableItems.get(item)) {
            if (!iBoxable.canBeStoredInToolbox(itemStack)) continue;
            return true;
        }
        return item instanceof IBoxable && ((IBoxable)item).canBeStoredInToolbox(itemStack);
    }

    public static void registerMetalArmor(Item item, IMetalArmor iMetalArmor) {
        metalArmorItems.put(item, iMetalArmor);
    }

    public static boolean isMetalArmor(ItemStack itemStack, Player player) {
        Item item = itemStack.getItem();
        for (IMetalArmor iMetalArmor : metalArmorItems.get(item)) {
            if (!iMetalArmor.isMetalArmor(itemStack, player)) continue;
            return true;
        }
        return item instanceof IMetalArmor && ((IMetalArmor)item).isMetalArmor(itemStack, player);
    }
}

