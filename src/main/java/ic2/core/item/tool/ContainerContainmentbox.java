/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package ic2.core.item.tool;

import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.item.tool.HandHeldContainmentbox;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotRadioactive;
import net.minecraft.world.entity.player.Inventory;

public class ContainerContainmentbox
extends ContainerHandHeldInventory<HandHeldContainmentbox> {
    protected static final int height = 166;

    public ContainerContainmentbox(int n, Inventory inventory, HandHeldContainmentbox box) {
        // 第三十三轮修复：与电表同一根因（DYNAMIC_ITEM 的界面工厂要求 DynamicContainer）。
        super(Ic2ScreenHandlers.CONTAINMENT_BOX, n, box);
        int i;
        for (i = 0; i < 4; ++i) {
            this.addSlot(new SlotRadioactive(box, i, 53 + i * 18, 19));
        }
        for (i = 4; i < 8; ++i) {
            this.addSlot(new SlotRadioactive(box, i, 53 + (i - 4) * 18, 37));
        }
        for (i = 8; i < 12; ++i) {
            this.addSlot(new SlotRadioactive(box, i, 53 + (i - 8) * 18, 55));
        }
        this.addPlayerInventorySlots(inventory, 166);
    }
}

