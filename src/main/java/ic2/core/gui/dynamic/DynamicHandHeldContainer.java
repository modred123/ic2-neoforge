/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.gui.dynamic;

import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DynamicHandHeldContainer<T extends HandHeldInventory>
extends DynamicContainer<T> {
    public static DynamicHandHeldContainer<HandHeldInventory> create(int n, Inventory inventory, HandHeldInventory handHeldInventory, GuiParser.GuiNode guiNode) {
        return new DynamicHandHeldContainer<HandHeldInventory>(Ic2ScreenHandlers.DYNAMIC_ITEM, n, inventory, handHeldInventory, guiNode);
    }

    public static <T extends HandHeldInventory> DynamicHandHeldContainer<T> create(MenuType<DynamicContainer<T>> menuType, int n, Inventory inventory, T t, GuiParser.GuiNode guiNode) {
        return new DynamicHandHeldContainer<T>(menuType, n, inventory, t, guiNode);
    }

    protected DynamicHandHeldContainer(MenuType<DynamicContainer<T>> menuType, int n, Inventory inventory, T t, GuiParser.GuiNode guiNode) {
        super(menuType, n, inventory, t, guiNode);
    }

    @Override
    protected SlotHologramSlot.ChangeCallback getCallback() {
        return ((HandHeldInventory)this.base).makeSaveCallback();
    }

    @Override
    public void onContainerEvent(String string) {
        ((HandHeldInventory)this.base).onEvent(string);
        super.onContainerEvent(string);
    }

    @Override
    public void clicked(int n, int n2, ClickType clickType, Player player) {
        ItemStack itemStack = null;
        boolean bl = false;
        Slot slot = null;
        if (!player.getCommandSenderWorld().isClientSide && n >= 0 && n < this.slots.size()) {
            slot = (Slot)this.slots.get(n);
            itemStack = slot.getItem();
            bl = ((HandHeldInventory)this.base).isThisContainer(itemStack);
        }
        super.clicked(n, n2, clickType, player);
        if (bl && !slot.hasItem()) {
            ((HandHeldInventory)this.base).saveAsThrown(itemStack);
            ((ServerPlayer)player).closeContainer();
        }
    }

    public void removed(Player player) {
        ((HandHeldInventory)this.base).onScreenClosed(player);
        super.removed(player);
    }
}

