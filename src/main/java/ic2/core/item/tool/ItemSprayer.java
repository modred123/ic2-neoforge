/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.fluids.Fluid
 *  net.minecraftforge.fluids.FluidStack
 *  net.minecraftforge.fluids.FluidUtil
 *  net.minecraftforge.fluids.capability.IFluidHandlerItem
 */
package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.wiring.CableBlock;
import ic2.core.block.wiring.FoamCableBlock;
import ic2.core.ref.Ic2Blocks;
import ic2.core.item.ItemIC2FluidContainer;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayDeque;
import java.util.HashSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class ItemSprayer
extends ItemIC2FluidContainer
implements IBoxable {
    public ItemSprayer() {
        super(ItemName.foam_sprayer, 8000);
        this.setMaxStackSize(1);
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
        if (!this.allowedIn(tab)) {
            return;
        }
        subItems.add(new ItemStack((Item)this));
        subItems.add(this.getItemStack(FluidName.construction_foam));
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (IC2.platform.isSimulating() && IC2.keyboard.isModeSwitchKeyDown(player)) {
            ItemStack stack = StackUtil.get(player, hand);
            CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
            int mode = nbtData.getInt("mode");
            mode = mode == 0 ? 1 : 0;
            nbtData.putInt("mode", mode);
            String sMode = mode == 0 ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single";
            IC2.platform.messagePlayer(player, "ic2.tooltip.mode", sMode);
        }
        return super.use(world, player, hand);
    }

    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction side, float xOffset, float yOffset, float zOffset) {
        Target target;
        ItemStack pack;
        BlockPos fluidPos;
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            return InteractionResult.PASS;
        }
        if (!IC2.platform.isSimulating()) {
            return InteractionResult.SUCCESS;
        }
        net.minecraft.world.phys.HitResult rtResult = ic2.core.util.Util.traceBlocks(player, true);
        if (rtResult == null) {
            return InteractionResult.PASS;
        }
        if (rtResult instanceof net.minecraft.world.phys.BlockHitResult && !pos.equals(((net.minecraft.world.phys.BlockHitResult)rtResult).getBlockPos()) && LiquidUtil.drainBlockToContainer(world, fluidPos = ((net.minecraft.world.phys.BlockHitResult)rtResult).getBlockPos(), player, hand)) {
            return InteractionResult.SUCCESS;
        }
        int maxFoamBlocks = 0;
        ItemStack stack = StackUtil.get(player, hand);
        FluidStack fluid = FluidUtil.getFluidContained((ItemStack)stack).orElse(null);
        if (fluid != null && fluid.getAmount() > 0) {
            maxFoamBlocks += fluid.getAmount() / this.getFluidPerFoam();
        }
        if ((pack = (ItemStack)player.getInventory().armor.get(2)) != null && pack.getItem() == ItemName.cf_pack.getInstance()) {
            fluid = FluidUtil.getFluidContained((ItemStack)pack).orElse(null);
            if (fluid != null && fluid.getAmount() > 0) {
                maxFoamBlocks += fluid.getAmount() / this.getFluidPerFoam();
            } else {
                pack = null;
            }
        } else {
            pack = null;
        }
        if (maxFoamBlocks == 0) {
            return InteractionResult.FAIL;
        }
        maxFoamBlocks = Math.min(maxFoamBlocks, this.getMaxFoamBlocks(stack));
        if (ItemSprayer.canPlaceFoam(world, pos, Target.Scaffold)) {
            target = Target.Scaffold;
        } else if (ItemSprayer.canPlaceFoam(world, pos, Target.Cable)) {
            target = Target.Cable;
        } else {
            pos = pos.relative(side);
            target = Target.Any;
        }
        Vec3 viewVec = player.getViewVector(1.0F);
        Direction playerViewFacing = Direction.getNearest(viewVec.x, viewVec.y, viewVec.z);
        int amount = this.sprayFoam(world, pos, playerViewFacing.getOpposite(), target, maxFoamBlocks);
        if ((amount *= this.getFluidPerFoam()) > 0) {
            if (pack != null) {
                IFluidHandlerItem packHandler = FluidUtil.getFluidHandler((ItemStack)pack).orElse(null);
                assert (packHandler != null);
                fluid = packHandler.drain(amount, FluidAction.EXECUTE);
                amount -= fluid.getAmount();
                player.getInventory().armor.set(2, packHandler.getContainer());
            }
            if (amount > 0) {
                IFluidHandlerItem handler = FluidUtil.getFluidHandler((ItemStack)stack).orElse(null);
                assert (handler != null);
                handler.drain(amount, FluidAction.EXECUTE);
                StackUtil.set(player, hand, handler.getContainer());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public int sprayFoam(Level world, BlockPos pos, Direction excludedDir, Target target, int maxFoamBlocks) {
        BlockPos cPos;
        if (!ItemSprayer.canPlaceFoam(world, pos, target)) {
            return 0;
        }
        ArrayDeque<BlockPos> toCheck = new ArrayDeque<BlockPos>();
        HashSet<BlockPos> positions = new HashSet<BlockPos>();
        toCheck.add(pos);
        while ((cPos = (BlockPos)toCheck.poll()) != null && positions.size() < maxFoamBlocks) {
            if (!ItemSprayer.canPlaceFoam(world, cPos, target) || !positions.add(cPos)) continue;
            for (Direction dir : Direction.values()) {
                if (dir == excludedDir) continue;
                toCheck.add(cPos.relative(dir));
            }
        }
        toCheck.clear();
        int failedPlacements = 0;
        for (BlockPos targetPos : positions) {
            BlockState state = world.getBlockState(targetPos);
            Block targetBlock = state.getBlock();
            if (ItemSprayer.isScaffold(targetBlock)) {
                world.destroyBlock(targetPos, true);
                world.setBlockAndUpdate(targetPos, Ic2Blocks.FOAM.defaultBlockState());
                continue;
            }
            if (targetBlock instanceof CableBlock && !(targetBlock instanceof FoamCableBlock)) {
                ++failedPlacements;
                continue;
            }
            if (world.setBlockAndUpdate(targetPos, Ic2Blocks.FOAM.defaultBlockState())) continue;
            ++failedPlacements;
        }
        return positions.size() - failedPlacements;
    }

    protected int getMaxFoamBlocks(ItemStack stack) {
        CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
        if (nbtData.getInt("mode") == 0) {
            return 10;
        }
        return 1;
    }

    protected int getFluidPerFoam() {
        return 100;
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemstack) {
        return true;
    }

    @Override
    public boolean canfill(Fluid fluid) {
        return fluid == FluidName.construction_foam.getInstance();
    }

    private static boolean isScaffold(Block block) {
        return block == Ic2Blocks.WOODEN_SCAFFOLD || block == Ic2Blocks.REINFORCED_WOODEN_SCAFFOLD
            || block == Ic2Blocks.IRON_SCAFFOLD || block == Ic2Blocks.REINFORCED_IRON_SCAFFOLD;
    }

    private static boolean canPlaceFoam(Level world, BlockPos pos, Target target) {
        switch (target) {
            case Any: {
                return Ic2Blocks.FOAM.defaultBlockState().canSurvive(world, pos);
            }
            case Scaffold: {
                return ItemSprayer.isScaffold(world.getBlockState(pos).getBlock());
            }
            case Cable: {
                Block block = world.getBlockState(pos).getBlock();
                return block instanceof CableBlock && !(block instanceof FoamCableBlock);
            }
            default: {
                assert (false);
                break;
            }
        }
        return false;
    }

    private static enum Target {
        Any,
        Scaffold,
        Cable;

    }
}

