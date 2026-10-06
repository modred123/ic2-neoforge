/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.DataSlot
 */
package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.block.personal.TileEntityEnergyOMat;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;

public class ContainerEnergyOMatClosed
extends ContainerFullInv<TileEntityEnergyOMat> {
    public ContainerEnergyOMatClosed(int n, Inventory inventory, TileEntityEnergyOMat tileEntityEnergyOMat) {
        super(Ic2ScreenHandlers.ENERGY_O_MAT_CLOSED, n, inventory, tileEntityEnergyOMat, 166);
        this.addSlot(new SlotInvSlotReadOnly(tileEntityEnergyOMat.demandSlot, 0, 50, 17));
        this.addSlot(new SlotInvSlot(tileEntityEnergyOMat.inputSlot, 0, 143, 17));
        this.addSlot(new SlotInvSlot(tileEntityEnergyOMat.chargeSlot, 0, 143, 53));
        this.addDataSlot(new DataSlot(){

            public int get() {
                return ((TileEntityEnergyOMat)ContainerEnergyOMatClosed.this.base).chargeSlot.tier;
            }

            public void set(int n) {
                ((TileEntityEnergyOMat)ContainerEnergyOMatClosed.this.base).chargeSlot.tier = n;
            }
        });
    }

    @Override
    public List<String> getNetworkedFields() {
        List<String> list = super.getNetworkedFields();
        list.add("paidFor");
        list.add("euOffer");
        return list;
    }
}

