/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Direction$AxisDirection
 *  net.minecraft.core.Registry
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.item.PriorityUsableItem;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemToolWrench
extends Item
implements PriorityUsableItem,
IBoxable {
    private static final boolean logEmptyWrenchDrops = ConfigUtil.getBool(MainConfig.get(), "debug/logEmptyWrenchDrops");
    public static final int wrenchUseFailed = -1;
    public static final int wrenchUsePass = -2;

    public ItemToolWrench(Item.Properties properties) {
        super(properties);
    }

    public boolean canTakeDamage(ItemStack itemStack, int n) {
        return true;
    }

        public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext useOnContext) {
        if (!this.canTakeDamage(itemStack, 1)) {
            return InteractionResult.FAIL;
        }
        Player player = useOnContext.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        int n = ItemToolWrench.onWrenchUse(player, itemStack, useOnContext, this.canTakeDamage(itemStack, 10));
        switch (n) {
            case -2: {
                return InteractionResult.PASS;
            }
            case -1: {
                return InteractionResult.FAIL;
            }
        }
        this.damage(itemStack, n, player, useOnContext.getHand());
        return InteractionResult.SUCCESS;
    }

    public static int onWrenchUse(Player player, ItemStack itemStack, UseOnContext useOnContext, boolean bl) {
        WrenchResult wrenchResult = ItemToolWrench.wrenchBlock(useOnContext.getLevel(), useOnContext.getClickedPos(), useOnContext.getClickedFace(), player, bl);
        if (wrenchResult != WrenchResult.Nothing) {
            if (!useOnContext.getLevel().isClientSide) {
                return wrenchResult == WrenchResult.Rotated ? 1 : 10;
            }
            player.playSound(Ic2SoundEvents.ITEM_WRENCH_USE, 1.0f, 1.0f);
            return -2;
        }
        return -1;
    }

    public static WrenchResult wrenchBlock(Level level, BlockPos blockPos, Direction direction, Player player, boolean bl) {
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (blockState.isAir()) {
            return WrenchResult.Nothing;
        }
        if (block instanceof IWrenchable) {
            Direction.Axis axis;
            Direction direction2;
            IWrenchable iWrenchable = (IWrenchable)block;
            Direction direction3 = direction2 = iWrenchable.getFacing(level, blockPos);
            if (IC2.keyboard.isAltKeyDown(player)) {
                axis = direction.getAxis();
                direction3 = ItemToolWrench.isAltRotationClockwise(direction, player) ? direction3.getClockWise(axis) : direction3.getCounterClockWise(axis);
            } else {
                direction3 = ItemToolWrench.getNewFacing(direction, player);
            }
            if (direction3 != direction2 && iWrenchable.setFacing(level, blockPos, direction3, player)) {
                return WrenchResult.Rotated;
            }
            if (bl && iWrenchable.wrenchCanRemove(level, blockPos, player)) {
                Object object;
                if (level.isClientSide) {
                    return WrenchResult.Removed;
                }
                if (player.blockActionRestricted(level, blockPos, ((ServerPlayer)player).gameMode.getGameModeForPlayer())) {
                    return WrenchResult.Nothing;
                }
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (ConfigUtil.getBool(MainConfig.get(), "protection/wrenchLogging")) {
                    String playerInfo = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
                    IC2.log.info(LogCategory.PlayerActivity, "Player %s used a wrench to remove the block %s (te %s) at %s.", playerInfo, blockState, ItemToolWrench.getTeName(blockEntity), Util.formatPosition(level, blockPos));
                }
                block.playerWillDestroy(level, blockPos, blockState, player);
                if (level.removeBlock(blockPos, false)) {
                    block.destroy(level, blockPos, blockState);
                }
                List<ItemStack> wrenchDrops = iWrenchable.getWrenchDrops(level, blockPos, blockState, blockEntity, player, 0);
                if (wrenchDrops == null || wrenchDrops.isEmpty()) {
                    if (logEmptyWrenchDrops) {
                        IC2.log.warn(LogCategory.General, "The block %s (te %s) at %s didn't yield any wrench drops.", blockState, ItemToolWrench.getTeName(blockEntity), Util.formatPosition(level, blockPos));
                    }
                } else {
                    for (ItemStack itemStack : wrenchDrops) {
                        StackUtil.dropAsEntity(level, blockPos, itemStack);
                    }
                }
                if (!player.getAbilities().instabuild) {
                    blockState.spawnAfterBreak((ServerLevel)level, blockPos, player.getUseItem(), false);
                }
                return WrenchResult.Removed;
            }
        } else {
            Direction direction4;
            Direction direction5;
            Property property;
            Rotation rotation = null;
            if (IC2.keyboard.isAltKeyDown(player)) {
                rotation = ItemToolWrench.isAltRotationClockwise(direction, player) ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
            } else if (direction.getAxis().isHorizontal() && (property = blockState.getBlock().getStateDefinition().getProperty("facing")) != null && property.getValueClass() == Direction.class && (direction5 = (Direction)blockState.getValue(property)) != null && direction5.getAxis().isHorizontal() && (direction4 = ItemToolWrench.getNewFacing(direction, player)) != direction5 && property.getPossibleValues().contains(direction4)) {
                rotation = direction5.getOpposite() == direction4 ? Rotation.CLOCKWISE_180 : (direction5.getClockWise(Direction.Axis.Y) == direction4 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
            }
            if (rotation != null) {
                BlockState rotated = IC2.envProxy.rotate(blockState, level, blockPos, rotation);
                if (rotated != blockState) {
                    level.setBlockAndUpdate(blockPos, rotated);
                    return WrenchResult.Rotated;
                }
            }
        }
        return WrenchResult.Nothing;
    }

    private static boolean isAltRotationClockwise(Direction direction, Player player) {
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE != player.isShiftKeyDown();
    }

    private static Direction getNewFacing(Direction direction, Player player) {
        return player.isShiftKeyDown() ? direction.getOpposite() : direction;
    }

    private static String getTeName(BlockEntity blockEntity) {
        return blockEntity != null ? BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()).toString() : "none";
    }

    public void damage(ItemStack itemStack, int n, Player player, InteractionHand interactionHand) {
        itemStack.hurtAndBreak(n, player, interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemStack) {
        return true;
    }

    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2 != null && itemStack2.is(Ic2ItemTags.BRONZE_INGOTS);
    }

    private static enum WrenchResult {
        Rotated,
        Removed,
        Nothing;

    }
}

