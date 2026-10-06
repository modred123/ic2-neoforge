/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.entity.LivingEntity
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
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.init.Localization;
import ic2.core.item.ItemToolIC2;
import ic2.core.item.tool.HarvestLevel;
import ic2.core.item.tool.ToolClass;
import ic2.core.ref.ItemName;
import ic2.core.util.RotationUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
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

public class ItemToolCrowbar
extends ItemToolIC2
implements IEnhancedOverlayProvider {
    public ItemToolCrowbar() {
        super(ItemName.crowbar, HarvestLevel.Iron, EnumSet.of(ToolClass.Crowbar));
        this.setMaxDamage(250);
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
        if (state.isAir()) {
            return InteractionResult.FAIL;
        }
        if (world.getBlockEntity(pos) instanceof ICoverHolder) {
            Direction selectedFacing;
            ICoverHolder target = (ICoverHolder)world.getBlockEntity(pos);
            if (target.canRemoveCover(world, pos, selectedFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ))) {
                if (!world.isClientSide) {
                    target.removeCover(world, pos, selectedFacing);
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                } else {
                    IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Crowbar.ogg", true, IC2.audioManager.getDefaultVolume());
                    IC2.platform.messagePlayer(player, Localization.translate("Attachment removed"), new Object[0]);
                }
            }
            return world.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
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
        tooltip.add(Component.literal(Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage().getString() + ":"));
        tooltip.add(Component.literal(" Remove attachments from blocks"));
    }

    @Override
    public boolean providesEnhancedOverlay(Level world, BlockPos pos, Direction side, Player player, ItemStack stack) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity instanceof ICoverHolder;
    }
}

