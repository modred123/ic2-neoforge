/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Registry
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.material.MapColor
 *  org.apache.commons.lang3.mutable.Mutable
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.fluid;

import ic2.core.IC2;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import java.util.Collection;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

public final class FluidHandler {
    static final EnvFluidHandler ENV_HANDLER = IC2.envProxy.createFluidStackHandler();

    public static EnvFluidHandler.FluidRefs createFluid(ResourceLocation resourceLocation, MapColor material, int n, int n2, int n3, int n4, boolean bl, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, int n5) {
        return ENV_HANDLER.createFluid(resourceLocation, material, n, n2, n3, n4, resourceLocation2, resourceLocation3, n5);
    }

    public static int getDensity(Fluid fluid) {
        return ENV_HANDLER.getDensity(fluid);
    }

    public static int getTemperature(Fluid fluid) {
        return ENV_HANDLER.getTemperature(fluid);
    }

    public static boolean isGaseous(Fluid fluid) {
        return ENV_HANDLER.isGaseous(fluid);
    }

    public static ResourceLocation getStillSpriteId(Fluid fluid) {
        return ENV_HANDLER.getStillSpriteId(fluid);
    }

    public static ResourceLocation getFlowingSpriteId(Fluid fluid) {
        return ENV_HANDLER.getFlowingSpriteId(fluid);
    }

    public static int getColor(Fluid fluid) {
        return ENV_HANDLER.getColor(fluid);
    }

    public static Ic2FluidStack drainMb(ItemStack itemStack, int n, boolean bl, @Nullable Mutable<ItemStack> mutable) {
        Item item = itemStack.getItem();
        if (item instanceof Ic2FluidItem) {
            return ((Ic2FluidItem)item).drainMb(itemStack, n, bl, mutable);
        }
        return ENV_HANDLER.drainMb(itemStack, n, bl, mutable);
    }

    public static int drainMb(ItemStack itemStack, Ic2FluidStack ic2FluidStack, boolean bl, Mutable<ItemStack> mutable) {
        Item item = itemStack.getItem();
        if (item instanceof Ic2FluidItem) {
            return ((Ic2FluidItem)item).drainMb(itemStack, ic2FluidStack, bl, mutable);
        }
        return ENV_HANDLER.drainMb(itemStack, ic2FluidStack, bl, mutable);
    }

    public static int fillMb(ItemStack itemStack, Ic2FluidStack ic2FluidStack, boolean bl, Mutable<ItemStack> mutable) {
        Item item = itemStack.getItem();
        if (item instanceof Ic2FluidItem) {
            return ((Ic2FluidItem)item).fillMb(itemStack, ic2FluidStack, bl, mutable);
        }
        return ENV_HANDLER.fillMb(itemStack, ic2FluidStack, bl, mutable);
    }

    public static boolean isFluidBlock(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.isFluidBlock(blockState, level, blockPos, null);
        }
        return ENV_HANDLER.isFluidBlock(blockState, level, blockPos, null, direction);
    }

    /**
     * 第五十五轮修复（P0 崩溃）：从 BlockEntity 补齐 level / blockPos。
     *
     * IC2 的流体工具有一批「只给 BlockEntity」的重载（isFluidBlock / drainMb / fillMb），
     * 迁移时把 level 与 blockPos 直接写成了 null。1.12.2 的 FluidUtil.getFluidHandler(world, pos, side)
     * 允许 null，但 1.21 的 NeoForge BlockCapability.getCapability(level, pos, ...) 会直接执行
     * pos.immutable() → NullPointerException（crash-2026-10-08_00.52.26：往流体储罐塞入「抽入升级」后，
     * TileEntityTank 每 tick 崩服）。
     *
     * 注：TE 尚未加入世界时 getLevel() 可能仍为 null，ENV 侧也会兜底。
     */
    private static Level levelOf(BlockEntity blockEntity) {
        return blockEntity == null ? null : blockEntity.getLevel();
    }

    private static BlockPos posOf(BlockEntity blockEntity) {
        return blockEntity == null ? null : blockEntity.getBlockPos();
    }

    public static boolean isFluidBlock(BlockEntity blockEntity, Direction direction) {
        if (blockEntity == null) {
            return false;
        }
        BlockState blockState = blockEntity.getBlockState();
        return FluidHandler.isFluidBlock(blockState, blockEntity, direction);
    }

    public static boolean isFluidBlock(BlockState blockState, BlockEntity blockEntity, Direction direction) {
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.isFluidBlock(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity);
        }
        return ENV_HANDLER.isFluidBlock(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity, direction);
    }

    public static Ic2FluidStack drainMb(BlockState blockState, Level level, BlockPos blockPos, Direction direction, int n, boolean bl) {
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.drainMb(blockState, level, blockPos, null, direction, n, bl);
        }
        return ENV_HANDLER.drainMb(blockState, level, blockPos, null, direction, n, bl);
    }

    public static Ic2FluidStack drainMb(BlockEntity blockEntity, Direction direction, int n, boolean bl) {
        BlockState blockState = blockEntity.getBlockState();
        return FluidHandler.drainMb(blockState, blockEntity, direction, n, bl);
    }

    public static Ic2FluidStack drainMb(BlockState blockState, BlockEntity blockEntity, Direction direction, int n, boolean bl) {
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.drainMb(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity, direction, n, bl);
        }
        return ENV_HANDLER.drainMb(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity, direction, n, bl);
    }

    public static int drainMb(BlockState blockState, Level level, BlockPos blockPos, Direction direction, Ic2FluidStack ic2FluidStack, boolean bl) {
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.drainMb(blockState, level, blockPos, null, direction, ic2FluidStack, bl);
        }
        return ENV_HANDLER.drainMb(blockState, level, blockPos, null, direction, ic2FluidStack, bl);
    }

    public static int drainMb(BlockEntity blockEntity, Direction direction, Ic2FluidStack ic2FluidStack, boolean bl) {
        BlockState blockState = blockEntity.getBlockState();
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.drainMb(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity, direction, ic2FluidStack, bl);
        }
        return ENV_HANDLER.drainMb(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity, direction, ic2FluidStack, bl);
    }

    public static int fillMb(BlockState blockState, Level level, BlockPos blockPos, Direction direction, Ic2FluidStack ic2FluidStack, boolean bl) {
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.fillMb(blockState, level, blockPos, null, direction, ic2FluidStack, bl);
        }
        return ENV_HANDLER.fillMb(blockState, level, blockPos, null, direction, ic2FluidStack, bl);
    }

    public static int fillMb(BlockEntity blockEntity, Direction direction, Ic2FluidStack ic2FluidStack, boolean bl) {
        BlockState blockState = blockEntity.getBlockState();
        Block block = blockState.getBlock();
        if (block instanceof Ic2FluidBlock) {
            Ic2FluidBlock ic2FluidBlock = (Ic2FluidBlock)block;
            return ic2FluidBlock.fillMb(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity, direction, ic2FluidStack, bl);
        }
        return ENV_HANDLER.fillMb(blockState, levelOf(blockEntity), posOf(blockEntity), blockEntity, direction, ic2FluidStack, bl);
    }

    public static Fluid getWorldFluid(BlockState blockState) {
        return ENV_HANDLER.getWorldFluid(blockState, null, null);
    }

    public static Fluid getWorldFluid(BlockState blockState, Level level, BlockPos blockPos) {
        return ENV_HANDLER.getWorldFluid(blockState, level, blockPos);
    }

    public static int getWorldFluidLevel(BlockState blockState, Level level, BlockPos blockPos) {
        return ENV_HANDLER.getWorldFluidLevel(blockState, level, blockPos);
    }

    public static Ic2FluidStack drainWorldFluid(BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
        return ENV_HANDLER.drainWorldFluid(blockState, level, blockPos, bl);
    }

    public static CompoundTag getFluidStackNbt(Ic2FluidStack ic2FluidStack) {
        return ENV_HANDLER.getFluidStackNbt(ic2FluidStack);
    }

    public static Ic2FluidStack createFluidStackMb(Fluid fluid, int n, CompoundTag compoundTag) {
        return ENV_HANDLER.createFluidStackMb(fluid, n, compoundTag);
    }

    public static Collection<Fluid> getAllFluids() {
        return BuiltInRegistries.FLUID.stream().filter((Predicate<Fluid>)FluidHandler::lambda$getAllFluids$0).toList();
    }

    private static /* synthetic */ boolean lambda$getAllFluids$0(Fluid fluid) {
        return fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY;
    }
}

