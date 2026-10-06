/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.Slot
 */
package ic2.core.item.tool;

import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.item.tool.HandHeldToolbox;
import ic2.core.slot.SlotBoxable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class ContainerToolbox
extends ContainerHandHeldInventory<HandHeldToolbox> {
    protected static final int height = 166;
    protected static final int windowBorder = 8;
    protected static final int slotSize = 16;
    protected static final int slotDistance = 2;
    protected static final int slotSeparator = 4;
    protected static final int hotbarYOffset = -24;
    protected static final int inventoryYOffset = -82;

    public ContainerToolbox(int n, net.minecraft.world.entity.player.Inventory inventory, HandHeldToolbox Toolbox1) {
        // 第三十三轮修复：与电表同一根因（DYNAMIC_ITEM 的界面工厂要求 DynamicContainer），工具箱右键同样会闪退。
        super(ic2.core.ref.Ic2ScreenHandlers.TOOLBOX, n, Toolbox1);
        int col;
        for (col = 0; col < 9; ++col) {
            this.addSlot(new SlotBoxable(Toolbox1, col, 8 + col * 18, 41));
        }
        for (int row = 0; row < 3; ++row) {
            for (int col2 = 0; col2 < 9; ++col2) {
                this.addSlot(new Slot(inventory, col2 + row * 9 + 9, 8 + col2 * 18, 84 + row * 18));
            }
        }
        for (col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }
}

