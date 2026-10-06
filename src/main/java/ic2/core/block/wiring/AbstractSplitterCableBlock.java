/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 */
package ic2.core.block.wiring;

import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.block.wiring.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class AbstractSplitterCableBlock
extends AbstractCableBlock {
    public static final BooleanProperty active = BooleanProperty.create((String)"active");

    protected AbstractSplitterCableBlock(BlockBehaviour.Properties properties, CableType cableType, int n) {
        super(properties, cableType, n);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(active, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{active});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)super.getStateForPlacement(blockPlaceContext).setValue(active, Boolean.valueOf(blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos())));
    }

    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        boolean bl2 = level.hasNeighborSignal(blockPos);
        if (blockState.getValue(active) == bl2) {
            return;
        }
        blockState = (BlockState)blockState.setValue(active, bl2);
        level.setBlockAndUpdate(blockPos, blockState);
        if (bl2) {
            this.addToEnet(blockState, level, blockPos, true);
        } else {
            this.removeFromEnet(blockState, level, blockPos);
        }
    }

    @Override
    protected void addToEnet(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
        if ((blockState.getValue(active)).booleanValue()) {
            super.addToEnet(blockState, level, blockPos, bl);
        }
    }
}

