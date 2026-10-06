/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.Tag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
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
import ic2.core.audio.PositionSpec;
import ic2.core.block.machine.tileentity.TileEntityITnt;
import ic2.core.entity.block.ITntEntity;
import ic2.core.ref.Ic2Blocks;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

public class ItemRemote
extends ItemIC2 {
    public ItemRemote() {
        super(ItemName.remote);
        this.setMaxStackSize(1);
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = StackUtil.get(player, context.getHand());
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block != Ic2Blocks.ITNT) {
            return InteractionResult.SUCCESS;
        }
        int index = ItemRemote.hasRemote(pos, stack);
        if (index > -1) {
            ItemRemote.removeRemote(index, stack);
            IC2.platform.messagePlayer(player, "Unlinked from this ITNT.", new Object[0]);
        } else {
            ItemRemote.addRemote(pos, stack);
            IC2.platform.messagePlayer(player, "Linked to this ITNT.", new Object[0]);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/dynamiteomote.ogg", true, IC2.audioManager.getDefaultVolume());
        ItemRemote.launchRemotes(world, stack, player);
        return InteractionResultHolder.success(stack);
    }

    public static void addRemote(BlockPos pos, ItemStack freq) {
        CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.contains("coords")) {
            compound.put("coords", new ListTag());
        }
        ListTag coords = compound.getList("coords", Tag.TAG_COMPOUND);
        CompoundTag coord = new CompoundTag();
        coord.putInt("x", pos.getX());
        coord.putInt("y", pos.getY());
        coord.putInt("z", pos.getZ());
        coords.add(coord);
        compound.put("coords", coords);
        freq.setDamageValue(coords.size());
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (stack.getDamageValue() > 0) {
            tooltip.add(Component.literal("Linked to " + stack.getDamageValue() + " dynamite"));
        }
    }

    public static void launchRemotes(Level world, ItemStack freq, Player player) {
        CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.contains("coords")) {
            return;
        }
        ListTag coords = compound.getList("coords", Tag.TAG_COMPOUND);
        int i = 0;
        while (i < coords.size()) {
            CompoundTag coord = coords.getCompound(i);
            BlockPos pos = new BlockPos(coord.getInt("x"), coord.getInt("y"), coord.getInt("z"));
            if (world.isLoaded(pos)) {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() == Ic2Blocks.ITNT) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof TileEntityITnt) {
                        ITntEntity dynamite = new ITntEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        dynamite.setCausingEntity(player);
                        world.removeBlock(pos, false);
                        world.addFreshEntity(dynamite);
                    }
                }
                coords.remove(i);
                continue;
            }
            ++i;
        }
        freq.setDamageValue(0);
    }

    public static int hasRemote(BlockPos pos, ItemStack freq) {
        CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.contains("coords")) {
            return -1;
        }
        ListTag coords = compound.getList("coords", Tag.TAG_COMPOUND);
        for (int i = 0; i < coords.size(); ++i) {
            CompoundTag coord = coords.getCompound(i);
            if (coord.getInt("x") != pos.getX() || coord.getInt("y") != pos.getY() || coord.getInt("z") != pos.getZ()) continue;
            return i;
        }
        return -1;
    }

    public static void removeRemote(int index, ItemStack freq) {
        CompoundTag compound = StackUtil.getOrCreateNbtData(freq);
        if (!compound.contains("coords")) {
            return;
        }
        ListTag coords = compound.getList("coords", Tag.TAG_COMPOUND);
        ListTag newCoords = new ListTag();
        for (int i = 0; i < coords.size(); ++i) {
            if (i == index) continue;
            newCoords.add(coords.getCompound(i));
        }
        compound.put("coords", newCoords);
        freq.setDamageValue(newCoords.size());
    }
}

