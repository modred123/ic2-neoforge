/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.tool;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.item.ItemWrapper;
import ic2.core.ContainerBase;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.tool.GuiToolbox;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.util.StackUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;

public class HandHeldToolbox
extends HandHeldInventory {
    public HandHeldToolbox(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack, int inventorySize) {
        super(player, hand, stack, inventorySize);
    }

    @Override
    public ContainerBase<HandHeldToolbox> createServerScreenHandler(int n, Player player) {
        return new ContainerToolbox(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<HandHeldToolbox> createClientScreenHandler(int n, net.minecraft.world.entity.player.Inventory inventory, ic2.core.network.GrowingBuffer growingBuffer) {
        return new ContainerToolbox(n, inventory, this);
    }

    public String getName() {
        return "toolbox";
    }

    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemstack) {
        if (StackUtil.isEmpty(itemstack)) {
            return false;
        }
        return ItemWrapper.canBeStoredInToolbox(itemstack);
    }
}

