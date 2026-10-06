/*
 * Decompiled with CFR 0.152.
 */
package ic2.core.block.comp;

import ic2.core.block.comp.BasicRedstoneComponent;
import ic2.core.block.tileentity.Ic2TileEntity;

public class ComparatorEmitter
extends BasicRedstoneComponent {
    public ComparatorEmitter(Ic2TileEntity ic2TileEntity) {
        super(ic2TileEntity);
    }

    @Override
    public void onChange() {
        this.parent.getLevel().updateNeighbourForOutputSignal(this.parent.getBlockPos(), this.parent.getBlockState().getBlock());
    }
}

