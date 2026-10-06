/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.fluids.FluidStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.logistics;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.block.transport.cover.CoverProperty;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.block.transport.cover.ICoverItem;
import ic2.core.init.Localization;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.ItemMulti;
import ic2.core.item.logistics.PumpCoverType;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.RotationUtil;
import ic2.core.util.StackUtil;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

public class ItemPumpCover
extends ItemMulti<PumpCoverType>
implements ICoverItem,
IEnhancedOverlayProvider {
    public ItemPumpCover() {
        super(ItemName.cover, PumpCoverType.class);
        this.setHasSubtypes(true);
        for (PumpCoverType type : PumpCoverType.values()) {
            ItemStack stack = new ItemStack(this, 1);
            stack.setDamageValue(type.getId());
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        float xOffset = (float)(context.getClickLocation().x - pos.getX());
        float yOffset = (float)(context.getClickLocation().y - pos.getY());
        float zOffset = (float)(context.getClickLocation().z - pos.getZ());
        InteractionHand hand = context.getHand();
        ItemStack stack = StackUtil.get(player, hand);
        PumpCoverType type = (PumpCoverType)this.getType(stack);
        if (type == null) {
            return InteractionResult.PASS;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (!(tileEntity instanceof ICoverHolder)) {
            return InteractionResult.PASS;
        }
        Direction selectedFacing = RotationUtil.rotateByHit(side, xOffset, yOffset, zOffset);
        if (((ICoverHolder)tileEntity).canPlaceCover(world, pos, selectedFacing, stack)) {
            if (!world.isClientSide) {
                ((ICoverHolder)tileEntity).placeCover(world, pos, selectedFacing, StackUtil.copyWithSize(stack, 1));
                stack.shrink(1);
            } else {
                IC2.platform.messagePlayer(player, Localization.translate("Attachment placed"), new Object[0]);
            }
        }
        return world.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        PumpCoverType type = (PumpCoverType)this.getType(stack);
        if (type == null) {
            return;
        }
        tooltip.add(Component.literal("Transfer rate: " + type.transferRate + " mb/sec (as attachment)"));
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, Set<CoverProperty> types) {
        PumpCoverType type = (PumpCoverType)this.getType(stack);
        if (type == null) {
            return false;
        }
        return types.contains((Object)CoverProperty.FluidConsuming);
    }

    @Override
    public boolean onTick(ItemStack stack, ICoverHolder parent) {
        PumpCoverType type = (PumpCoverType)this.getType(stack);
        if (type == null) {
            return false;
        }
        CompoundTag nbtTagCompound = StackUtil.getOrCreateNbtData(stack);
        Direction side = Direction.from3DDataValue(nbtTagCompound.getByte("side") & 0xFF);
        boolean ret = false;
        BlockEntity holder = (BlockEntity)parent;
        LiquidUtil.AdjacentFluidHandler target = LiquidUtil.getAdjacentHandler(holder, side);
        if (target != null) {
            int amount = type.transferRate / 20;
            LiquidUtil.transfer(target.handler, target.dir.getOpposite(), holder, amount);
        }
        return ret;
    }

    @Override
    public boolean allowsInput(ItemStack stack) {
        return false;
    }

    @Override
    public boolean allowsInput(Ic2FluidStack stack) {
        return true;
    }

    @Override
    public boolean allowsOutput(ItemStack stack) {
        return false;
    }

    @Override
    public boolean allowsOutput(Ic2FluidStack stack) {
        return false;
    }

    @Override
    public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity instanceof ICoverHolder;
    }

    private static String getSideName(Direction dir) {
        switch (dir) {
            case WEST: {
                return "ic2.dir.west";
            }
            case EAST: {
                return "ic2.dir.east";
            }
            case DOWN: {
                return "ic2.dir.bottom";
            }
            case UP: {
                return "ic2.dir.top";
            }
            case NORTH: {
                return "ic2.dir.north";
            }
            case SOUTH: {
                return "ic2.dir.south";
            }
        }
        throw new RuntimeException("Invalid direction: " + dir);
    }
}

