/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.CriteriaTriggers
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.stats.Stats
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BucketItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.ItemUtils
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.BucketPickup
 *  net.minecraft.world.level.block.LiquidBlock
 *  net.minecraft.world.level.block.LiquidBlockContainer
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.material.FlowingFluid
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.neoforged.neoforge.event.ForgeEventFactory
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.item;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public abstract class Ic2BucketItem
extends BucketItem {
    protected Fluid fluid;
    private final List<Fluid> drainableFluidList;

    public Ic2BucketItem(Fluid fluid, Item.Properties properties) {
        super(fluid, properties);
        this.fluid = fluid;
        this.drainableFluidList = this.getDrainableFluidList();
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        BlockPos blockPos;
        BlockHitResult blockHitResult;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        blockHitResult = Ic2BucketItem.getPlayerPOVHitResult(level, player, this.fluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }
        BlockPos blockPos2 = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getDirection();
        BlockPos blockPos3 = blockPos2.relative(direction);
        if (!level.mayInteract(player, blockPos2) || !player.mayUseItemAt(blockPos3, direction, itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (this.fluid == Fluids.EMPTY) {
            BlockState blockState = level.getBlockState(blockPos2);
            Block block = blockState.getBlock();
            if (block instanceof BucketPickup) {
                BucketPickup bucketPickup = (BucketPickup)block;
                ItemStack drainedStack = this.tryDrainFluid(level, blockPos2, blockState);
                if (!drainedStack.isEmpty()) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos2);
                    ItemStack itemStack2 = ItemUtils.createFilledResult(itemStack, player, drainedStack);
                    if (!level.isClientSide) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, drainedStack);
                    }
                    return InteractionResultHolder.sidedSuccess(itemStack2, level.isClientSide());
                }
            }
            return InteractionResultHolder.fail(itemStack);
        }
        BlockState blockState = level.getBlockState(blockPos2);
        BlockPos blockPos4 = blockPos = this.canBlockContainFluid(level, blockPos2, blockState) ? blockPos2 : blockPos3;
        if (this.bucketUseOnBlock(new UseOnContext(player, interactionHand, blockHitResult))) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (this.emptyContents(player, level, blockPos, blockHitResult)) {
            this.checkExtraContent(player, level, itemStack, blockPos);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            ItemStack itemStack3 = ItemUtils.createFilledResult((ItemStack)itemStack, (Player)player, (ItemStack)this.getEmptiedBucketStack(itemStack, player));
            return InteractionResultHolder.sidedSuccess(itemStack3, level.isClientSide());
        }
        return InteractionResultHolder.fail(itemStack);
    }

    private ItemStack getEmptiedBucketStack(ItemStack itemStack, Player player) {
        if (!player.getAbilities().instabuild) {
            return new ItemStack(this.getEmptiedBucketItem());
        }
        return itemStack;
    }

    public ItemStack tryDrainFluid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        Fluid fluid;
        if (blockState.getBlock() instanceof LiquidBlock && this.drainableFluidList.contains(fluid = blockState.getFluidState().getType())) {
            levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            return new ItemStack(this.getBucketItem(fluid));
        }
        if (blockState.getBlock() instanceof SimpleWaterloggedBlock && (blockState.getValue(BlockStateProperties.WATERLOGGED)).booleanValue()) {
            levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            if (!blockState.canSurvive((LevelReader)levelAccessor, blockPos)) {
                levelAccessor.destroyBlock(blockPos, true);
            }
            return new ItemStack(this.getBucketItem(Fluids.WATER));
        }
        return this.tryDrain(levelAccessor, blockPos, blockState);
    }

    public abstract Item getEmptiedBucketItem();

    public abstract List<Fluid> getDrainableFluidList();

    public abstract Item getBucketItem(Fluid var1);

    public ItemStack tryDrain(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    public boolean bucketUseOnBlock(UseOnContext useOnContext) {
        return false;
    }

    public boolean emptyContents(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        if (!(this.fluid instanceof FlowingFluid)) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        MapColor material = blockState.getMapColor(level, blockPos);
        boolean bl = blockState.canBeReplaced(this.fluid);
        boolean bl2 = blockState.isAir() || bl || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(player, level, blockPos, blockState, this.fluid);
        boolean bl3 = bl2;
        if (!bl2) {
            return blockHitResult != null && this.emptyContents(player, level, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null);
        }
        if (level.dimensionType().ultraWarm() && this.fluid.is(FluidTags.WATER)) {
            int n = blockPos.getX();
            int n2 = blockPos.getY();
            int n3 = blockPos.getZ();
            level.playSound(player, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
            for (int i = 0; i < 8; ++i) {
                level.addParticle((ParticleOptions)ParticleTypes.LARGE_SMOKE, (double)n + Math.random(), (double)n2 + Math.random(), (double)n3 + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }
        if (block instanceof LiquidBlockContainer && this.fluid == Fluids.WATER) {
            ((LiquidBlockContainer)block).placeLiquid((LevelAccessor)level, blockPos, blockState, ((FlowingFluid)this.fluid).getSource(false));
            this.playEmptySound(player, (LevelAccessor)level, blockPos);
            return true;
        }
        if (!level.isClientSide && bl && blockState.getFluidState().isEmpty()) {
            level.destroyBlock(blockPos, true);
        }
        if (level.setBlock(blockPos, this.fluid.defaultFluidState().createLegacyBlock(), 11) || blockState.getFluidState().isSource()) {
            this.playEmptySound(player, (LevelAccessor)level, blockPos);
            return true;
        }
        return false;
    }

    private boolean canBlockContainFluid(Level level, BlockPos blockPos, BlockState blockState) {
        return blockState.getBlock() instanceof LiquidBlockContainer && ((LiquidBlockContainer)blockState.getBlock()).canPlaceLiquid(null, level, blockPos, blockState, this.fluid);
    }

    protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
        SoundEvent soundEvent = this.fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        levelAccessor.playSound(player, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
        levelAccessor.gameEvent((Entity)player, GameEvent.FLUID_PLACE, blockPos);
    }

}

