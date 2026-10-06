/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 */
package ic2.core.item.tool;

import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.item.tool.HandHeldCropnalyzer;
import ic2.core.ref.ItemName;
import ic2.core.slot.SlotCustom;
import ic2.core.slot.SlotDischarge;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

public class ContainerCropnalyzer
extends ContainerHandHeldInventory<HandHeldCropnalyzer> {
    public ContainerCropnalyzer(int n, Inventory inventory, HandHeldCropnalyzer cropnalyzer1) {
        super(Ic2ScreenHandlers.CROP_ANALYZER, n, cropnalyzer1);
        this.addSlot(new SlotCustom(cropnalyzer1, (Item)ItemName.crop_seed_bag.getInstance(), 0, 8, 7));
        this.addSlot(new SlotCustom(cropnalyzer1, null, 1, 41, 7));
        this.addSlot(new SlotDischarge(cropnalyzer1, 2, 152, 7));
        this.addPlayerInventorySlots(inventory, 223);
    }
}

