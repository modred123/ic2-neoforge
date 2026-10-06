/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package ic2.core.item;

import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class ItemTinCan
extends Item {
    public ItemTinCan(Item.Properties properties) {
        super(properties);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = StackUtil.get(player, interactionHand);
        if (!level.isClientSide && player.getFoodData().needsFood()) {
            return this.onEaten(player, itemStack);
        }
        return new InteractionResultHolder(InteractionResult.PASS, (Object)itemStack);
    }

    public InteractionResultHolder<ItemStack> onEaten(Player player, ItemStack itemStack) {
        int n = Math.min(StackUtil.getSize(itemStack), 20 - player.getFoodData().getFoodLevel());
        if (n <= 0) {
            return new InteractionResultHolder(InteractionResult.PASS, (Object)itemStack);
        }
        ItemStack itemStack2 = new ItemStack((ItemLike)Ic2Items.TIN_CAN, n);
        if (StackUtil.storeInventoryItem(itemStack2, player, true)) {
            player.getFoodData().eat(n, (float)n);
            itemStack = StackUtil.decSize(itemStack, n);
            StackUtil.storeInventoryItem(itemStack2, player, false);
            return new InteractionResultHolder(InteractionResult.SUCCESS, (Object)itemStack);
        }
        return new InteractionResultHolder(InteractionResult.PASS, (Object)itemStack);
    }
}

