/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.tool;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

public class ItemFrequencyTransmitter
extends ItemIC2 {
    private static final String targetSetNbt = "targetSet";
    private static final String targetJustSetNbt = "targetJustSet";
    private static final String targetXNbt = "targetX";
    private static final String targetYNbt = "targetY";
    private static final String targetZNbt = "targetZ";

    public ItemFrequencyTransmitter() {
        super(ItemName.frequency_transmitter);
        this.setMaxStackSize(1);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (IC2.platform.isSimulating()) {
            CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
            boolean hadJustSet = nbtData.getBoolean(targetJustSetNbt);
            if (nbtData.getBoolean(targetSetNbt) && !hadJustSet) {
                nbtData.putBoolean(targetSetNbt, false);
                IC2.platform.messagePlayer(player, "Frequency Transmitter unlinked", new Object[0]);
            }
            if (hadJustSet) {
                nbtData.putBoolean(targetJustSetNbt, false);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        Level world = useOnContext.getLevel();
        BlockPos pos = useOnContext.getClickedPos();
        Direction side = useOnContext.getClickedFace();
        InteractionHand hand = useOnContext.getHand();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof TileEntityTeleporter)) {
            return InteractionResult.PASS;
        }
        TileEntityTeleporter tp = (TileEntityTeleporter)te;
        CompoundTag nbtData = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
        boolean targetSet = nbtData.getBoolean(targetSetNbt);
        boolean justSetTarget = true;
        BlockPos target = new BlockPos(nbtData.getInt(targetXNbt), nbtData.getInt(targetYNbt), nbtData.getInt(targetZNbt));
        if (!targetSet) {
            targetSet = true;
            target = tp.getBlockPos();
            IC2.platform.messagePlayer(player, "Frequency Transmitter linked to Teleporter.", new Object[0]);
        } else if (tp.getBlockPos().equals((Object)target)) {
            IC2.platform.messagePlayer(player, "Can't link Teleporter to itself.", new Object[0]);
        } else if (tp.hasTarget() && tp.getTarget().equals((Object)target)) {
            IC2.platform.messagePlayer(player, "Teleportation link unchanged.", new Object[0]);
        } else {
            BlockEntity targetTe = world.getBlockEntity(target);
            if (targetTe instanceof TileEntityTeleporter) {
                tp.setTarget(target);
                ((TileEntityTeleporter)targetTe).setTarget(pos);
                IC2.platform.messagePlayer(player, "Teleportation link established.", new Object[0]);
            } else {
                justSetTarget = false;
                targetSet = false;
            }
        }
        nbtData.putBoolean(targetSetNbt, targetSet);
        nbtData.putBoolean(targetJustSetNbt, justSetTarget);
        nbtData.putInt(targetXNbt, target.getX());
        nbtData.putInt(targetYNbt, target.getY());
        nbtData.putInt(targetZNbt, target.getZ());
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
        if (nbtData.getBoolean(targetSetNbt)) {
            tooltip.add(Component.literal(Localization.translate("ic2.frequency_transmitter.tooltip.target", nbtData.getInt(targetXNbt), nbtData.getInt(targetYNbt), nbtData.getInt(targetZNbt))));
        } else {
            tooltip.add(Component.literal(Localization.translate("ic2.frequency_transmitter.tooltip.blank")));
        }
    }
}

