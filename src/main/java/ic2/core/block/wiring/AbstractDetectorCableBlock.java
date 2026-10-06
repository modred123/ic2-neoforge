/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.block.wiring.CableType;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class AbstractDetectorCableBlock
extends AbstractCableBlock {
    public static final BooleanProperty active = BooleanProperty.create((String)"active");
    private static final int tickRate = 32;

    protected AbstractDetectorCableBlock(BlockBehaviour.Properties properties) {
        super(properties, CableType.detector, 0);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(active, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{active});
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
        level.scheduleTick(blockPos, (Block)this, level.getRandom().nextInt(32));
    }

    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        boolean bl;
        serverLevel.scheduleTick(blockPos, (Block)this, 32);
        IEnergyTile iEnergyTile = EnergyNet.instance.getTile((Level)serverLevel, blockPos);
        if (iEnergyTile == null) {
            return;
        }
        NodeStats nodeStats = EnergyNet.instance.getNodeStats(iEnergyTile);
        if (nodeStats == null) {
            return;
        }
        boolean bl2 = bl = nodeStats.getEnergyIn() > 0.0;
        if (bl != blockState.getValue(active)) {
            serverLevel.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(active, bl));
        } else if (bl) {
            serverLevel.updateNeighbourForOutputSignal(blockPos, (Block)this);
        }
    }

    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(active) != false ? 15 : 0;
    }

    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (!(blockState.getValue(active)).booleanValue()) {
            return 0;
        }
        IEnergyTile iEnergyTile = EnergyNet.instance.getTile(level, blockPos);
        if (iEnergyTile == null) {
            return 0;
        }
        return (int)Util.map(EnergyNet.instance.getNodeStats(iEnergyTile).getEnergyIn() / (double)this.type.capacity, 1.0, 15.0);
    }
}

