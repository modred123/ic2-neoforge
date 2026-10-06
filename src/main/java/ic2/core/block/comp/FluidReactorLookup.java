/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core.block.comp;

import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.util.Util;
import ic2.core.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluidReactorLookup
extends TileEntityComponent {
    private TileEntityNuclearReactorElectric reactor;
    private long lastReactorUpdate;

    public FluidReactorLookup(Ic2TileEntity ic2TileEntity) {
        super(ic2TileEntity);
    }

    public TileEntityNuclearReactorElectric getReactor() {
        long l = this.parent.getLevel().getGameTime();
        if (l != this.lastReactorUpdate) {
            this.updateReactor();
            this.lastReactorUpdate = l;
        } else if (this.reactor != null && (this.reactor.isRemoved() || !this.reactor.isFluidCooled())) {
            this.reactor = null;
        }
        return this.reactor;
    }

    private void updateReactor() {
        BlockPos blockPos;
        Level level = this.parent.getLevel();
        if (!Util.isAreaLoaded((LevelReader)level, blockPos = this.parent.getBlockPos(), 2)) {
            this.reactor = null;
            return;
        }
        if (this.reactor != null && !this.reactor.isRemoved() && this.reactor.isFluidCooled() && this.reactor.getLevel() == level && level.getBlockEntity(this.reactor.getBlockPos()) == this.reactor) {
            BlockPos blockPos2 = this.reactor.getBlockPos();
            int n = Math.abs(blockPos.getX() - blockPos2.getX());
            int n2 = Math.abs(blockPos.getY() - blockPos2.getY());
            int n3 = Math.abs(blockPos.getZ() - blockPos2.getZ());
            if (n <= 2 && n2 <= 2 && n3 <= 2 && (n == 2 || n2 == 2 || n3 == 2)) {
                return;
            }
        }
        this.reactor = null;
        WorldUtil.findTileEntities(level, blockPos, 2, new WorldUtil.ITileEntityResultHandler(){

            @Override
            public boolean onMatch(BlockEntity blockEntity) {
                TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric;
                if (blockEntity instanceof TileEntityNuclearReactorElectric && (tileEntityNuclearReactorElectric = (TileEntityNuclearReactorElectric)blockEntity).isFluidCooled()) {
                    FluidReactorLookup.this.reactor = tileEntityNuclearReactorElectric;
                    return true;
                }
                return false;
            }
        });
    }
}

