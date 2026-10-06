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

import ic2.core.ContainerBase;
import ic2.core.item.reactor.ItemReactorMOX;
import ic2.core.item.reactor.ItemReactorUranium;
import ic2.core.item.tool.ContainerContainmentbox;
import ic2.core.item.tool.GuiContainmentbox;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.ref.ItemName;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;

public class HandHeldContainmentbox
extends HandHeldInventory {
    public HandHeldContainmentbox(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack1, int inventorySize) {
        super(player, hand, stack1, inventorySize);
    }

    @Override
    public ContainerBase<HandHeldContainmentbox> createServerScreenHandler(int n, Player player) {
        return new ContainerContainmentbox(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<HandHeldContainmentbox> createClientScreenHandler(int n, net.minecraft.world.entity.player.Inventory inventory, ic2.core.network.GrowingBuffer growingBuffer) {
        return new ContainerContainmentbox(n, inventory, this);
    }

    public String getName() {
        return "ic2.containment_box";
    }

    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getItem() == ItemName.nuclear.getInstance() || stack.getItem() instanceof ItemReactorMOX || stack.getItem() instanceof ItemReactorUranium;
    }
}

