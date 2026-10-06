/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.LocalPlayer
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.tool;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.tile.IWrenchable;
import ic2.api.transport.IPipe;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.audio.PositionSpec;
import ic2.core.item.ItemToolIC2;
import ic2.core.item.tool.HarvestLevel;
import ic2.core.item.tool.ToolClass;
import ic2.core.ref.ItemName;
import ic2.core.util.RotationUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;

public class ItemToolWrenchNew
extends ItemToolIC2
implements IEnhancedOverlayProvider,
IHitSoundOverride {
    public ItemToolWrenchNew() {
        super(ItemName.wrench_new, HarvestLevel.Iron, EnumSet.of(ToolClass.Wrench));
        this.setMaxDamage(120);
    }

    public boolean canTakeDamage(ItemStack stack, int amount) {
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        Level world = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();
        Direction side = useOnContext.getClickedFace();
        InteractionHand hand = useOnContext.getHand();
        float hitX = (float)useOnContext.getClickLocation().x;
        float hitY = (float)useOnContext.getClickLocation().y;
        float hitZ = (float)useOnContext.getClickLocation().z;
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = StackUtil.get(player, hand);
        if (!this.canTakeDamage(stack, 1)) {
            return InteractionResult.FAIL;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()) {
            return InteractionResult.FAIL;
        }
        if (world.getBlockEntity(pos) instanceof IPipe) {
            IPipe target = (IPipe)world.getBlockEntity(pos);
            Direction newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
            assert (target != null);
            target.flipConnection(newFacing);
            if (world.getBlockEntity(pos.relative(newFacing)) instanceof IPipe) {
                IPipe other = (IPipe)world.getBlockEntity(pos.relative(newFacing));
                assert (other != null);
                if (target.isConnected(newFacing) != other.isConnected(newFacing.getOpposite())) {
                    other.flipConnection(newFacing.getOpposite());
                }
            }
            if (world.isClientSide) {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
            }
            return InteractionResult.SUCCESS;
        }
        if (block instanceof IWrenchable) {
            IWrenchable wrenchable = (IWrenchable)block;
            Direction newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
            wrenchable.setFacing(world, pos, newFacing, player);
            if (world.isClientSide) {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair != null && Util.matchesOD(repair, "ingotBronze");
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage().getString() + ":"));
        tooltip.add(Component.literal(" Safely mine IC2 machines (Yes you will get the machine and not the machine block)"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal(Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage().getString() + ":"));
        tooltip.add(Component.literal(" Set the machine facing (rotate)"));
        tooltip.add(Component.literal(" Connect pipes together and to covers"));
    }

    @Override
    public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IWrenchable) {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            return tileEntity instanceof IPipe || Arrays.stream(Direction.values()).anyMatch(face -> ((IWrenchable)block).canSetFacing(world, pos, (Direction)face, player));
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public net.minecraft.sounds.SoundEvent getHitSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack) {
        return ic2.core.ref.Ic2SoundEvents.ITEM_WRENCH_USE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public net.minecraft.sounds.SoundEvent getBreakSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return null;
        }
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof IWrenchable ? ic2.core.ref.Ic2SoundEvents.ITEM_WRENCH_USE : null;
    }
}

