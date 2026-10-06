/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.monster.Ravager
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.IntegerProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.block.tileentity;

import com.google.common.base.Suppliers;
import ic2.api.block.BreakableBlock;
import ic2.api.crops.CropSoilType;
import ic2.api.tile.IWrenchable;
import ic2.api.tile.RetexturableBlock;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.crop.Ic2CropType;
import ic2.core.crop.TileEntityCrop;
import ic2.core.util.Util;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class Ic2TileEntityBlock
extends Block
implements EntityBlock,
IWrenchable,
BreakableBlock,
RetexturableBlock {
    private static final BlockEntityTicker<Ic2TileEntity> TICKER = (level, blockPos, blockState, ic2TileEntity) -> ic2TileEntity.tick();
    private static final String facingPropertyName = "facing";
    public static final Property<Direction> anyFacingProperty = DirectionProperty.create((String)"facing", Util.allFacings);
    public static final Property<Direction> horizontalFacingProperty = DirectionProperty.create((String)"facing", Util.horizontalFacings);
    public static final Property<Direction> verticalFacingProperty = DirectionProperty.create((String)"facing", Util.verticalFacings);
    public static final BooleanProperty CROSSING_BASE = BooleanProperty.create((String)"crossing_base");
    private static final Map<Integer, IntegerProperty> ageProperties = new HashMap<Integer, IntegerProperty>();
    private int cropMaxAge = -1;
    private Ic2CropType cropType;
    private static final ThreadLocal<InitData> pendingInitData = new ThreadLocal();
    private final Class<? extends Ic2TileEntity> teClass;
    private final boolean canActive;
    private final DefaultDrop defaultDrop;
    private final boolean allowWrenchRotating;
    private final Set<Direction> supportedFacings;
    private final Supplier<Ic2TileEntity> dummyTe;
    public final Property<Direction> facingProperty;
    private boolean enableWorldTick;
    public static final BooleanProperty ACTIVE = BooleanProperty.create((String)"active");

    public static Ic2TileEntityBlock create(BlockBehaviour.Properties properties, Class<? extends Ic2TileEntity> clazz, boolean bl, DefaultDrop defaultDrop, Set<Direction> set, boolean bl2) {
        InitData initData = new InitData(set, bl, clazz, null, -1);
        pendingInitData.set(initData);
        Ic2TileEntityBlock ic2TileEntityBlock = new Ic2TileEntityBlock(properties, clazz, bl, initData, defaultDrop, bl2);
        pendingInitData.remove();
        return ic2TileEntityBlock;
    }

    public static Ic2TileEntityBlock create(BlockBehaviour.Properties properties, Class<? extends Ic2TileEntity> clazz, boolean bl, DefaultDrop defaultDrop, Set<Direction> set, boolean bl2, Ic2CropType ic2CropType) {
        InitData initData = new InitData(set, bl, clazz, ic2CropType, ic2CropType.getMaxAge());
        pendingInitData.set(initData);
        Ic2TileEntityBlock ic2TileEntityBlock = new Ic2TileEntityBlock(properties, clazz, bl, initData, defaultDrop, bl2, ic2CropType);
        pendingInitData.remove();
        return ic2TileEntityBlock;
    }

    private Ic2TileEntityBlock(BlockBehaviour.Properties properties, Class<? extends Ic2TileEntity> clazz, boolean bl, InitData initData, DefaultDrop defaultDrop, boolean bl2, Ic2CropType ic2CropType) {
        this(properties, clazz, bl, initData, defaultDrop, bl2);
        if (clazz.equals(TileEntityCrop.class)) {
            this.cropMaxAge = ic2CropType.getMaxAge();
            this.cropType = ic2CropType;
        }
    }

    private Ic2TileEntityBlock(BlockBehaviour.Properties properties, Class<? extends Ic2TileEntity> clazz, boolean bl, InitData initData, DefaultDrop defaultDrop, boolean bl2) {
        super(properties);
        assert (initData == pendingInitData.get());
        this.teClass = clazz;
        this.canActive = bl;
        this.defaultDrop = defaultDrop;
        this.allowWrenchRotating = bl2;
        this.supportedFacings = initData.supportedFacings;
        this.facingProperty = this.supportedFacings.size() > 1 ? (Property<Direction>)this.stateDefinition.getProperty(facingPropertyName) : null;
        this.dummyTe = Suppliers.memoize(this::createDummyTe);
    }

    private IntegerProperty getAgeProperty(int n) {
        if (ageProperties.containsKey(n)) {
            return ageProperties.get(n);
        }
        IntegerProperty integerProperty = IntegerProperty.create((String)"age", (int)0, (int)n);
        ageProperties.put(n, integerProperty);
        return integerProperty;
    }

    public IntegerProperty getAgeProperty() {
        if (this.cropMaxAge == -1) {
            return null;
        }
        return this.getAgeProperty(this.cropMaxAge);
    }

    public int getCropMaxAge() {
        return this.cropMaxAge;
    }

    public Ic2CropType getCropType() {
        return this.cropType;
    }

    public Class<? extends Ic2TileEntity> getTeClass() {
        return this.teClass;
    }

    public boolean canActive() {
        return this.canActive;
    }

    public void setActive(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        if (!this.canActive()) {
            return;
        }
        if (!blockState.is((Block)this)) {
            return;
        }
        Ic2TileEntity ic2TileEntity = (Ic2TileEntity)level.getBlockEntity(blockPos);
        if (ic2TileEntity == null) {
            return;
        }
        ic2TileEntity.setActive(bl);
        level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(ACTIVE, Boolean.valueOf(bl)));
    }

    public DefaultDrop getDefaultDrop() {
        return this.defaultDrop;
    }

    public boolean allowWrenchRotating() {
        return this.allowWrenchRotating;
    }

    public Set<Direction> getSupportedFacings() {
        return this.supportedFacings;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return this.createBlockEntity(blockPos, blockState);
    }

    public Ic2TileEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        try {
            return this.teClass.getConstructor(BlockPos.class, BlockState.class).newInstance(blockPos, blockState);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return (BlockEntityTicker<T>)(BlockEntityTicker<?>)TICKER;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        InitData initData = pendingInitData.get();
        Set<Direction> set = initData.supportedFacings;
        if (set.size() > 1) {
            if (set.equals(Util.allFacings)) {
                builder.add(new Property[]{anyFacingProperty});
            } else if (set.equals(Util.horizontalFacings)) {
                builder.add(new Property[]{horizontalFacingProperty});
            } else if (set.equals(Util.verticalFacings)) {
                builder.add(new Property[]{verticalFacingProperty});
            } else {
                builder.add(new Property[]{DirectionProperty.create((String)facingPropertyName, set)});
            }
        }
        if (initData.canActive) {
            builder.add(new Property[]{ACTIVE});
        }
        if (initData.cropType != null) {
            if (initData.cropType == Ic2CropType.none) {
                builder.add(new Property[]{CROSSING_BASE});
            } else {
                builder.add(new Property[]{this.getAgeProperty(initData.maxAge)});
            }
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = super.getStateForPlacement(blockPlaceContext);
        if (blockState == null) {
            return null;
        }
        if (this.facingProperty != null) {
            blockState = blockState.setValue(this.facingProperty, this.getPlacementFacing((LivingEntity)blockPlaceContext.getPlayer(), blockPlaceContext.getNearestLookingDirection()));
        }
        if (this.canActive) {
            blockState = (BlockState)blockState.setValue(ACTIVE, Boolean.valueOf(false));
        }
        if (this.cropType != null) {
            blockState = this.cropType == Ic2CropType.none ? (BlockState)blockState.setValue(CROSSING_BASE, Boolean.valueOf(false)) : (BlockState)blockState.setValue((Property)this.getAgeProperty(), Integer.valueOf(0));
        }
        return blockState;
    }

    private Direction getPlacementFacing(LivingEntity livingEntity, Direction direction) {
        Set<Direction> set = this.getSupportedFacings();
        if (set.isEmpty()) {
            return Direction.DOWN;
        }
        if (livingEntity == null) {
            return direction != null && set.contains(direction.getOpposite()) ? direction.getOpposite() : this.getSupportedFacings().iterator().next();
        }
        Vec3 vec3 = livingEntity.getLookAngle();
        Direction direction2 = null;
        double d = Double.NEGATIVE_INFINITY;
        for (Direction direction3 : set) {
            double d2 = vec3.dot(Vec3.atLowerCornerOf((Vec3i)direction3.getOpposite().getNormal()));
            if (!(d2 > d)) continue;
            d = d2;
            direction2 = direction3;
        }
        return direction2;
    }

    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if (this.cropType == null) {
            return super.canSurvive(blockState, levelReader, blockPos);
        }
        return CropSoilType.contains(levelReader.getBlockState(blockPos.below()).getBlock()) && super.canSurvive(blockState, levelReader, blockPos);
    }

    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (this.cropType == null) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        }
        if (!blockState.canSurvive((LevelReader)levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return;
        }
        ic2TileEntity.onNeighborChange(block, blockPos2);
    }

    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (this.cropType != null && entity instanceof Ravager && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            level.destroyBlock(blockPos, true, entity);
        }
        super.entityInside(blockState, level, blockPos, entity);
    }

    private void updateStateForEnergyNet(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity) {
        if (this.supportedFacings.size() <= 1) {
            return;
        }
        Direction[] directionArray = new Direction[this.supportedFacings.size()];
        directionArray = this.supportedFacings.toArray(directionArray);
        Direction direction = (Direction)blockState.getValue(this.facingProperty);
        Direction direction2 = direction.equals((Object)directionArray[0]) ? directionArray[1] : directionArray[0];
        this.setFacing(level, blockPos, direction2, (Player)livingEntity);
        this.setFacing(level, blockPos, direction, (Player)livingEntity);
    }

    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return;
        }
        this.updateStateForEnergyNet(level, blockPos, blockState, livingEntity);
        ic2TileEntity.onPlaced(itemStack, livingEntity, this.facingProperty != null ? (Direction)blockState.getValue(this.facingProperty) : Direction.NORTH);
    }

    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe(blockGetter, blockPos);
        if (ic2TileEntity == null) {
            if (!this.hasCollision) {
                return Shapes.empty();
            }
            return Shapes.block();
        }
        return ic2TileEntity.getOutlineShape(collisionContext);
    }

    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe(blockGetter, blockPos);
        if (ic2TileEntity == null) {
            if (!this.hasCollision) {
                return Shapes.empty();
            }
            return Shapes.block();
        }
        return ic2TileEntity.getCollisionShape(collisionContext);
    }

    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe(blockGetter, blockPos);
        if (ic2TileEntity == null) {
            if (!this.hasCollision) {
                return Shapes.empty();
            }
            return Shapes.block();
        }
        return ic2TileEntity.getCullingShape();
    }

    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return InteractionResult.PASS;
        }
        return ic2TileEntity.onActivated(player, InteractionHand.MAIN_HAND, blockHitResult.getDirection(), blockHitResult.getLocation());
    }

    @Override
    public InteractionResult startBreak(Player player, Level level, InteractionHand interactionHand, BlockPos blockPos, BlockState blockState, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof Ic2TileEntity)) {
            return InteractionResult.PASS;
        }
        Ic2TileEntity ic2TileEntity = (Ic2TileEntity)blockEntity;
        return ic2TileEntity.onClicked(player);
    }

    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        if (blockEntity instanceof Ic2TileEntity && itemStack.getItem().isCorrectToolForDrops(itemStack, blockState)) {
            Ic2TileEntityBlock.popResource((Level)level, (BlockPos)blockPos, (ItemStack)((Ic2TileEntity)blockEntity).adjustDrop(blockState.getBlock().asItem().getDefaultInstance(), false));
        }
    }

    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.is(blockState2.getBlock()) || bl) {
            super.onRemove(blockState, level, blockPos, blockState2, bl);
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof Ic2TileEntity)) {
            return;
        }
        Ic2TileEntity ic2TileEntity = (Ic2TileEntity)blockEntity;
        for (ItemStack itemStack : ic2TileEntity.getAuxDrops(0)) {
            Block.popResource((Level)level, (BlockPos)blockPos, (ItemStack)itemStack);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public Direction getFacing(Level level, BlockPos blockPos) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return Direction.DOWN;
        }
        return ic2TileEntity.getFacing();
    }

    @Override
    public boolean canSetFacing(Level level, BlockPos blockPos, Direction direction, Player player) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return false;
        }
        return ic2TileEntity.canSetFacingWrench(direction, player);
    }

    @Override
    public boolean setFacing(Level level, BlockPos blockPos, Direction direction, Player player) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return false;
        }
        return ic2TileEntity.setFacingWrench(level, direction, player);
    }

    @Override
    public boolean wrenchCanRemove(Level level, BlockPos blockPos, Player player) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return false;
        }
        return ic2TileEntity.wrenchCanRemove(player);
    }

    @Override
    public List<ItemStack> getWrenchDrops(Level level, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, Player player, int n) {
        ItemStack itemStack = blockState.getBlock().asItem().getDefaultInstance();
        if (blockEntity instanceof Ic2TileEntity) {
            itemStack = ((Ic2TileEntity)blockEntity).adjustDrop(itemStack, true);
        }
        return Collections.singletonList(itemStack);
    }

    @Override
    public boolean retexture(BlockState blockState, Level level, BlockPos blockPos, Direction direction, Player player, BlockState blockState2, String string, Direction direction2, int[] nArray) {
        Ic2TileEntity ic2TileEntity = Ic2TileEntityBlock.getTe((BlockGetter)level, blockPos);
        if (ic2TileEntity == null) {
            return false;
        }
        Obscuration obscuration = ic2TileEntity.getComponent(Obscuration.class);
        return obscuration == null ? false : obscuration.applyObscuration(direction, new Obscuration.ObscurationData(blockState2, string, direction2, nArray));
    }

    private static Ic2TileEntity getTe(BlockGetter blockGetter, BlockPos blockPos) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof Ic2TileEntity) {
            return (Ic2TileEntity)blockEntity;
        }
        return null;
    }

    public Ic2TileEntity getDummyTe() {
        return this.dummyTe.get();
    }

    private Ic2TileEntity createDummyTe() {
        return this.createBlockEntity(BlockPos.ZERO, this.defaultBlockState());
    }

    private record InitData(Set<Direction> supportedFacings, boolean canActive, Class<?> teClass, Ic2CropType cropType, int maxAge) {
    }

    public static enum DefaultDrop {
        Self,
        None,
        Generator,
        Machine,
        AdvMachine;

    }
}

