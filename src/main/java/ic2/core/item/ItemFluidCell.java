/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.BlockDispenser
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.fluids.DispenseFluidContainer
 *  net.minecraftforge.fluids.Fluid
 *  net.minecraftforge.fluids.FluidUtil
 *  net.minecraftforge.fluids.capability.IFluidHandler
 *  net.minecraftforge.fluids.capability.IFluidHandlerItem
 */
package ic2.core.item;

import ic2.core.IC2;
import ic2.core.item.ItemIC2FluidContainer;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.DispenseFluidContainer;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class ItemFluidCell
extends ItemIC2FluidContainer {
    public ItemFluidCell() {
        super(ItemName.fluid_cell, 1000);
        DispenserBlock.DISPENSER_REGISTRY.put((net.minecraft.world.item.Item)this, net.neoforged.neoforge.fluids.DispenseFluidContainer.getInstance());
    }

    @Override
    public boolean isRepairable(ItemStack itemStack) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (this.interactWithTank(player, hand, world, pos, side)) {
            player.inventoryMenu.broadcastChanges();
            return InteractionResult.SUCCESS;
        }
        net.minecraft.world.phys.HitResult position = Util.traceBlocks(player, true);
        if (position == null || !(position instanceof net.minecraft.world.phys.BlockHitResult blockHit)) {
            return InteractionResult.FAIL;
        }
        pos = blockHit.getBlockPos();
        if (!world.mayInteract(player, pos)) {
            return InteractionResult.FAIL;
        }
        if (LiquidUtil.drainBlockToContainer(world, pos, player, hand) ) {
            player.inventoryMenu.broadcastChanges();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public boolean canfill(Fluid fluid) {
        return true;
    }

    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
        if (!this.allowedIn(tab) || false) {
            return;
        }
        ItemStack emptyStack = new ItemStack((Item)this);
        subItems.add(emptyStack);
        for (Fluid fluid : LiquidUtil.getAllFluidsSorted()) {
            ItemStack stack;
            if (fluid == null || (stack = this.getItemStack(fluid)) == null) continue;
            subItems.add(stack);
        }
    }

    private boolean interactWithTank(Player player, InteractionHand hand, Level world, BlockPos pos, Direction side) {
        boolean single;
        assert (!world.isClientSide);
        IFluidHandler tileHandler = FluidUtil.getFluidHandler((Level)world, (BlockPos)pos, (Direction)side).orElse(null);
        if (tileHandler == null) {
            return false;
        }
        ItemStack stack = StackUtil.get(player, hand);
        boolean bl = single = StackUtil.getSize(stack) == 1;
        if (!single) {
            stack = StackUtil.copyWithSize(stack, 1);
        }
        boolean changeMade = false;
        do {
            IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler((ItemStack)StackUtil.copy(stack)).orElse(null);
            assert (itemHandler != null);
            if (FluidUtil.tryFluidTransfer((IFluidHandler)tileHandler, (IFluidHandler)itemHandler, (int)Integer.MAX_VALUE, (boolean)true) == null) break;
            if (single) {
                StackUtil.set(player, hand, itemHandler.getContainer());
                return true;
            }
            StackUtil.consumeOrError(player, hand, 1);
            StackUtil.storeInventoryItem(itemHandler.getContainer(), player, false);
            changeMade = true;
        } while (!StackUtil.isEmpty(player, hand));
        return changeMade;
    }
}

