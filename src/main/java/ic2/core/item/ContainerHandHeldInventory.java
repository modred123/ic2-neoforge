/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item;

import ic2.core.ContainerBase;
import ic2.core.item.tool.HandHeldInventory;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerHandHeldInventory<T extends HandHeldInventory>
extends ContainerBase<T> {
    public ContainerHandHeldInventory(MenuType<?> menuType, int n, T t) {
        super(menuType, n, ((HandHeldInventory)t).player.getInventory(), t);
    }

    @Override
    public void clicked(int n, int n2, ClickType clickType, Player player) {
        ItemStack itemStack;
        ItemStack itemStack2 = null;
        boolean bl = false;
        switch (clickType) {
            case CLONE: {
                break;
            }
            case PICKUP: 
            case THROW: {
                if (n < 0 || n >= this.slots.size()) break;
                itemStack2 = ((Slot)this.slots.get(n)).getItem();
                bl = ((HandHeldInventory)this.base).isThisContainer(itemStack2);
                break;
            }
            case PICKUP_ALL: {
                break;
            }
            case QUICK_CRAFT: {
                break;
            }
            case QUICK_MOVE: {
                if (n < 0 || n >= this.slots.size() || !((HandHeldInventory)this.base).isThisContainer(((Slot)this.slots.get(n)).getItem())) break;
                return;
            }
            case SWAP: {
                assert (n >= 0 && n < this.slots.size());
                int n3 = this.findSlot((Container)player.getInventory(), n2).orElse(-1);
                assert (n3 >= 0);
                int n4 = -1;
                if (((HandHeldInventory)this.base).isThisContainer(player.getInventory().getItem(n2))) {
                    Slot slot = (Slot)this.slots.get(n);
                    int n5 = slot.getContainerSlot();
                    if (slot.container == player.getInventory() && n5 >= 0 && n5 < 9) {
                        n4 = n5;
                    }
                } else if (((HandHeldInventory)this.base).isThisContainer(((Slot)this.slots.get(n)).getItem())) {
                    n4 = n2;
                }
                if (n4 < 0 || !(player instanceof ServerPlayer)) break;
                ((ServerPlayer)player).connection.send((Packet)new ClientboundSetCarriedItemPacket(n4));
                break;
            }
            default: {
                throw new RuntimeException("Unexpected ClickType: " + clickType);
            }
        }
        super.clicked(n, n2, clickType, player);
        if (bl && !player.getCommandSenderWorld().isClientSide) {
            assert (itemStack2 != null);
            ((HandHeldInventory)this.base).saveAsThrown(itemStack2);
            ((ServerPlayer)player).closeContainer();
        } else if (clickType == ClickType.CLONE && ((HandHeldInventory)this.base).isThisContainer(itemStack = this.getCarried())) {
            ic2.core.util.StackUtil.getOrCreateNbtData(itemStack).remove("uid");
        }
    }

    public void removed(Player player) {
        ((HandHeldInventory)this.base).onScreenClosed(player);
        super.removed(player);
    }
}

