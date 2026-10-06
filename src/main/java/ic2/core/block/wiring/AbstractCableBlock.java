/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMap
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.PipeBlock
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IColoredEnergyTile;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.core.block.ChunkLoadAwareBlock;
import ic2.core.block.wiring.CableFoam;
import ic2.core.block.wiring.CableType;
import ic2.core.block.wiring.FoamCableBlock;
import ic2.core.item.tool.ItemToolCutter;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2Fluids;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractCableBlock
extends PipeBlock
implements ChunkLoadAwareBlock,
SimpleWaterloggedBlock {
    public static final DyeColor DEFAULT_COLOR = DyeColor.BLACK;
    public static final EnumProperty<DyeColor> colorProperty = EnumProperty.create((String)"color", DyeColor.class);
    public static final EnumProperty<CableFoam> foamProperty = EnumProperty.create((String)"foam", CableFoam.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    /** 诊断用（2026-09-23）：电缆入网只在首次打一条日志，避免刷屏。 */
    private static final java.util.concurrent.atomic.AtomicBoolean diagCableEnetLogged = new java.util.concurrent.atomic.AtomicBoolean(false);
    private static final Map<CableType, Int2ReferenceMap<AbstractCableBlock>> types = new EnumMap<CableType, Int2ReferenceMap<AbstractCableBlock>>(CableType.class);
    private static boolean pendingHasColor;
    private boolean hasColor;
    final CableType type;
    final int insulation;

    public abstract boolean isFoam();

    public abstract boolean isHardFoam(BlockState var1);

    public void initializeState(BlockState blockState) {
        if (this.isFoam()) {
            blockState = (BlockState)blockState.setValue(foamProperty, FoamCableBlock.DEFAULT_FOAM);
        } else {
            blockState = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(UP, Boolean.valueOf(false))).setValue(DOWN, Boolean.valueOf(false))).setValue(NORTH, Boolean.valueOf(false))).setValue(EAST, Boolean.valueOf(false))).setValue(SOUTH, Boolean.valueOf(false))).setValue(WEST, Boolean.valueOf(false));
            if (this.hasColor()) {
                blockState = (BlockState)blockState.setValue(colorProperty, DEFAULT_COLOR);
            }
        }
        this.registerDefaultState(blockState);
    }

    protected BlockState copyState(BlockState blockState, AbstractCableBlock abstractCableBlock) {
        BlockState blockState2 = abstractCableBlock.defaultBlockState();
        if (abstractCableBlock.insulation >= abstractCableBlock.type.minColoredInsulation) {
            blockState2 = (BlockState)blockState2.setValue(colorProperty, ((AbstractCableBlock)blockState.getBlock()).getColor(blockState));
        }
        blockState2 = this.isFoam() ? (BlockState)blockState2.setValue(foamProperty, blockState.getValue(foamProperty)) : (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)blockState2.setValue(UP, blockState.getValue(UP))).setValue(DOWN, blockState.getValue(DOWN))).setValue(NORTH, blockState.getValue(NORTH))).setValue(EAST, blockState.getValue(EAST))).setValue(SOUTH, blockState.getValue(SOUTH))).setValue(WEST, blockState.getValue(WEST));
        return blockState2;
    }

    protected static void prepareCreate(CableType cableType, int n) {
        pendingHasColor = n >= cableType.minColoredInsulation;
    }

    protected AbstractCableBlock(BlockBehaviour.Properties properties, CableType cableType, int n) {
        super(cableType.getThickness(n) / 2.0f, properties);
        if (n > cableType.maxInsulation) {
            throw new IllegalArgumentException("invalid insulation " + n + " for type " + cableType);
        }
        this.type = cableType;
        this.insulation = n;
        this.hasColor = n >= cableType.minColoredInsulation;
        BlockState blockState = (BlockState)this.stateDefinition.any();
        if (!this.isFoam()) {
            blockState = (BlockState)blockState.setValue(WATERLOGGED, Boolean.valueOf(false));
        }
        this.initializeState(blockState);
        if (cableType.maxInsulation > 0) {
            types.computeIfAbsent(cableType, AbstractCableBlock::createTypeMap).put(n, this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected MapCodec<? extends AbstractCableBlock> codec() {
        return (MapCodec<? extends AbstractCableBlock>)(MapCodec<?>)Block.CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (this.isFoam()) {
            builder.add(new Property[]{foamProperty});
        } else {
            builder.add(new Property[]{WATERLOGGED, UP, DOWN, NORTH, EAST, SOUTH, WEST});
        }
        if (pendingHasColor) {
            builder.add(new Property[]{colorProperty});
        }
    }

    public FluidState getFluidState(BlockState blockState) {
        if (!this.isFoam() && (blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    public BlockState withConnectionStates(BlockState blockState, Level level, BlockPos blockPos) {
        boolean bl = this.isConnectedWith(blockState, null, level, blockPos.below());
        boolean bl2 = this.isConnectedWith(blockState, null, level, blockPos.above());
        boolean bl3 = this.isConnectedWith(blockState, null, level, blockPos.north());
        boolean bl4 = this.isConnectedWith(blockState, null, level, blockPos.east());
        boolean bl5 = this.isConnectedWith(blockState, null, level, blockPos.south());
        boolean bl6 = this.isConnectedWith(blockState, null, level, blockPos.west());
        return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(DOWN, Boolean.valueOf(bl))).setValue(UP, Boolean.valueOf(bl2))).setValue(NORTH, Boolean.valueOf(bl3))).setValue(EAST, Boolean.valueOf(bl4))).setValue(SOUTH, Boolean.valueOf(bl5))).setValue(WEST, Boolean.valueOf(bl6));
    }

    public boolean isConnectedWith(BlockState blockState, BlockState blockState2, Level level, BlockPos blockPos) {
        Block block;
        if (blockState2 == null) {
            blockState2 = level.getBlockState(blockPos);
        }
        if ((block = blockState2.getBlock()) instanceof AbstractCableBlock) {
            AbstractCableBlock abstractCableBlock = (AbstractCableBlock)block;
            if (!abstractCableBlock.hasColor() || !this.hasColor()) {
                return true;
            }
            return AbstractCableBlock.getColor(blockState2, abstractCableBlock.type, abstractCableBlock.insulation) == this.getColor(blockState);
        }
        return blockState2.is(Ic2BlockTags.CABLE_CONNECTABLE);
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = this.defaultBlockState();
        Fluid fluid = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos()).getType();
        if (fluid == Fluids.WATER) {
            blockState = (BlockState)blockState.setValue(WATERLOGGED, Boolean.valueOf(true));
        }
        if (!this.isFoam()) {
            blockState = this.withConnectionStates(blockState, blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
        } else if (fluid == Ic2Fluids.CONSTRUCTION_FOAM.still) {
            blockState = (BlockState)blockState.setValue(foamProperty, CableFoam.SOFT);
        }
        return blockState;
    }

    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        if (!this.isFoam()) {
            return super.getOcclusionShape(blockState, blockGetter, blockPos);
        }
        if (((CableFoam)((Object)blockState.getValue(foamProperty))).isSoft()) {
            return Shapes.empty();
        }
        return super.getOcclusionShape(blockState, blockGetter, blockPos);
    }

    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (!this.isFoam()) {
            return super.getShape(blockState, blockGetter, blockPos, collisionContext);
        }
        if (((CableFoam)((Object)blockState.getValue(foamProperty))).isPresent()) {
            return Shapes.block();
        }
        return Shapes.empty();
    }

    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!this.isFoam() && (blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.scheduleTick(blockPos, (Fluid)Fluids.WATER, Fluids.WATER.getTickDelay((LevelReader)levelAccessor));
        }
        if (!this.isFoam()) {
            boolean bl = this.isConnectedWith(blockState, blockState2, (Level)levelAccessor, blockPos2);
            return (BlockState)blockState.setValue((Property)PipeBlock.PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(bl));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public boolean canPlaceLiquid(Player player, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return !this.isFoam() && blockState.getValue(WATERLOGGED) == false && fluid == Fluids.WATER;
    }

    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!this.canPlaceLiquid(null, (BlockGetter)levelAccessor, blockPos, blockState, fluidState.getType())) {
            return false;
        }
        if (!levelAccessor.isClientSide()) {
            levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(WATERLOGGED, Boolean.valueOf(true)), 3);
            levelAccessor.scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay((LevelReader)levelAccessor));
        }
        return true;
    }

    @Override
    public ItemStack pickupBlock(Player player, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (this.isFoam() || !(blockState.getValue(WATERLOGGED)).booleanValue()) {
            return ItemStack.EMPTY;
        }
        levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(WATERLOGGED, Boolean.valueOf(false)), 3);
        return new ItemStack((ItemLike)Items.WATER_BUCKET);
    }

    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (this.isHardFoam(blockState)) {
            return;
        }
        ItemStack itemStack = player.getMainHandItem();
        Item item = itemStack.getItem();
        if (item instanceof ItemToolCutter) {
            ((ItemToolCutter)item).removeInsulation(player, player.getUsedItemHand(), blockState, level, blockPos);
        }
    }

    public boolean hasColor() {
        return this.hasColor;
    }

    DyeColor getColor(BlockState blockState) {
        return AbstractCableBlock.getColor(blockState, this.type, this.insulation);
    }

    public static DyeColor getColor(BlockState blockState, CableType cableType, int n) {
        if (n >= cableType.minColoredInsulation) {
            return (DyeColor)blockState.getValue(colorProperty);
        }
        return DEFAULT_COLOR;
    }

    public boolean tryAddInsulation(BlockState blockState, Level level, BlockPos blockPos) {
        if (this.insulation >= this.type.maxInsulation) {
            return false;
        }
        if (this.isHardFoam(blockState)) {
            return false;
        }
        AbstractCableBlock abstractCableBlock = (AbstractCableBlock)types.get((Object)this.type).get(this.insulation + 1);
        if (abstractCableBlock == null) {
            return false;
        }
        level.setBlockAndUpdate(blockPos, this.copyState(blockState, abstractCableBlock));
        return true;
    }

    public boolean tryRemoveInsulation(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
        if (this.insulation <= 0) {
            return false;
        }
        if (this.isHardFoam(blockState)) {
            return false;
        }
        AbstractCableBlock abstractCableBlock = (AbstractCableBlock)types.get((Object)this.type).get(this.insulation - 1);
        if (abstractCableBlock == null) {
            return false;
        }
        if (bl) {
            return true;
        }
        BlockState blockState2 = this.copyState(blockState, abstractCableBlock);
        IEnergyTile iEnergyTile = null;
        if (this.insulation == this.type.minColoredInsulation && this.getColor(blockState) != DEFAULT_COLOR) {
            assert (abstractCableBlock.getColor(blockState2) == DEFAULT_COLOR);
            if (!level.isClientSide && (iEnergyTile = EnergyNet.instance.getTile(level, blockPos)) != null) {
                EnergyNet.instance.removeTile(iEnergyTile);
            }
        }
        level.setBlockAndUpdate(blockPos, blockState2);
        if (iEnergyTile != null) {
            EnergyNet.instance.addTileUnchecked(iEnergyTile);
        }
        return true;
    }

    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState2.is((Block)this) || this.getColor(blockState2) != this.getColor(blockState)) {
            this.removeFromEnet(blockState, level, blockPos);
        }
    }

    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        this.addToEnet(blockState, level, blockPos, true);
    }

    @Override
    public void onLoad(BlockState blockState, Level level, BlockPos blockPos) {
        this.addToEnet(blockState, level, blockPos, false);
    }

    @Override
    public void onUnload(BlockState blockState, Level level, BlockPos blockPos) {
        this.removeFromEnet(blockState, level, blockPos);
    }

    protected void addToEnet(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
        // 1.21 迁移修复（第十八轮）：能量网是服务端专属结构（EnergyNetGlobal.getLocal 对客户端
        // 抛 "not applicable clientside"），而 1.21 客户端方块预测也会触发 onPlace/onRemove。
        // 原版 Forge 1.19 靠调用点恰好不触发躲过，NeoForge 下必须显式守卫。
        if (level.isClientSide) {
            return;
        }
        // 诊断（2026-09-23，一次性）：确认电缆放置/加载时真的走到入网调用，以及 EnergyNet 实现类。
        if (diagCableEnetLogged.compareAndSet(false, true)) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").info(
                "cable addToEnet FIRST call: pos={} enetImpl={}",
                blockPos, EnergyNet.instance == null ? "null" : EnergyNet.instance.getClass().getSimpleName());
        }
        if (bl && EnergyNet.instance.getTile(level, blockPos) != null) {
            return;
        }
        EnergyNet.instance.addLocatableTile(new Conductor(blockState, level, blockPos));
    }

    protected void removeFromEnet(BlockState blockState, Level level, BlockPos blockPos) {
        if (level.isClientSide) {
            return;
        }
        IEnergyTile iEnergyTile = EnergyNet.instance.getTile(level, blockPos);
        if (iEnergyTile != null) {
            EnergyNet.instance.removeTile(iEnergyTile);
        }
    }

    private static Int2ReferenceMap<AbstractCableBlock> createTypeMap(CableType cableType) {
        return new Int2ReferenceOpenHashMap(cableType.maxInsulation + 1);
    }

    private final class Conductor
    implements ILocatable,
    IColoredEnergyTile,
    IEnergyConductor {
        private BlockState state;
        private final Level world;
        private final BlockPos pos;

        Conductor(BlockState blockState, Level level, BlockPos blockPos) {
            this.state = blockState;
            this.world = level;
            this.pos = blockPos.immutable();
        }

        @Override
        public Level getWorldObj() {
            return this.world;
        }

        @Override
        public BlockPos getPosition() {
            return this.pos;
        }

        @Override
        public DyeColor getColor(Direction direction) {
            return AbstractCableBlock.getColor(this.state, AbstractCableBlock.this.type, AbstractCableBlock.this.insulation);
        }

        @Override
        public boolean acceptsEnergyFrom(IEnergyEmitter iEnergyEmitter, Direction direction) {
            return this.canInteractWith(iEnergyEmitter, direction);
        }

        @Override
        public boolean emitsEnergyTo(IEnergyAcceptor iEnergyAcceptor, Direction direction) {
            return this.canInteractWith(iEnergyAcceptor, direction);
        }

        @Override
        public double getConductionLoss() {
            return AbstractCableBlock.this.type.loss;
        }

        @Override
        public double getInsulationEnergyAbsorption() {
            if (AbstractCableBlock.this.type.maxInsulation == 0) {
                return 2.147483647E9;
            }
            if (AbstractCableBlock.this.type.capacity < 128) {
                return EnergyNet.instance.getPowerFromTier(AbstractCableBlock.this.insulation);
            }
            return EnergyNet.instance.getPowerFromTier(AbstractCableBlock.this.insulation + 1);
        }

        @Override
        public double getInsulationBreakdownEnergy() {
            return 9001.0;
        }

        @Override
        public double getConductorBreakdownEnergy() {
            return AbstractCableBlock.this.type.capacity + 1;
        }

        @Override
        public void removeInsulation() {
            AbstractCableBlock.this.tryRemoveInsulation(this.state, this.world, this.pos, false);
        }

        @Override
        public void removeConductor() {
            this.world.removeBlock(this.pos, false);
        }

        @Override
        public void onConnectionChange() {
        }

        private void setState(BlockState blockState) {
            assert (blockState != this.state);
            this.state = blockState;
            this.world.setBlockAndUpdate(this.pos, blockState);
        }
    }
}

