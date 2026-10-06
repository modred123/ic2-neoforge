/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.wiring.tileentity;

import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityElectricCESU
extends TileEntityElectricBlock {
    /** 储电上限（EU）。创造栏"满电版本"（Ic2CreativeVariants）写 NBT "energy" 时复用此常量。 */
    public static final int CAPACITY = 300000;

    public TileEntityElectricCESU(BlockPos blockPos, BlockState blockState) {
        super(Ic2BlockEntities.CESU, blockPos, blockState, 2, 128, CAPACITY);
    }
}

