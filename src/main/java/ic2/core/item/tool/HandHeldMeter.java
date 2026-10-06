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
import ic2.core.item.tool.ContainerMeter;
import ic2.core.item.tool.GuiToolMeter;
import ic2.core.item.tool.HandHeldInventory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;

public class HandHeldMeter
extends HandHeldInventory {
    public HandHeldMeter(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        super(player, hand, stack, 0);
    }

    @Override
    public ContainerBase<?> createServerScreenHandler(int n, Player player) {
        return new ContainerMeter(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, net.minecraft.world.entity.player.Inventory inventory, ic2.core.network.GrowingBuffer growingBuffer) {
        return new ContainerMeter(n, inventory, this);
    }

    public String getName() {
        return "ic2.meter";
    }

    public boolean hasCustomName() {
        return false;
    }

    void closeGUI() {
        this.player.closeContainer();
    }
}

