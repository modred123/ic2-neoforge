/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.gameevent.GameEvent$Context
 */
package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.block.misc.RubberLogBlock;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2GameEvents;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ItemTreetap
extends Item
implements IBoxable {
    public ItemTreetap(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        Block block = blockState.getBlock();
        if (block == Ic2Blocks.RUBBER_LOG) {
            Player player = useOnContext.getPlayer();
            if (ItemTreetap.attemptExtract(player, level, blockPos, useOnContext.getClickedFace(), blockState, null, false)) {
                StackUtil.damage(player, useOnContext.getHand(), StackUtil.anyStack, 1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static boolean attemptExtract(Player player, Level level, BlockPos blockPos, Direction direction, BlockState blockState, List<ItemStack> list, boolean bl) {
        assert (blockState.getBlock() == Ic2Blocks.RUBBER_LOG);
        RubberLogBlock.RubberWoodState rubberWoodState = (RubberLogBlock.RubberWoodState)((Object)blockState.getValue(RubberLogBlock.stateProperty));
        if (rubberWoodState.isPlain() || rubberWoodState.facing != direction) {
            return false;
        }
        if (rubberWoodState.wet) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(RubberLogBlock.stateProperty, rubberWoodState.getDry()));
                if (list != null) {
                    list.add(StackUtil.copyWithSize(new ItemStack((ItemLike)Ic2Items.RESIN), level.random.nextInt(3) + 1));
                } else {
                    ItemTreetap.ejectResin(level, blockPos, direction, level.random.nextInt(3) + 1);
                }
            }
            ItemTreetap.triggerToolUseEvent(level, blockPos, player, blockState, bl);
            return true;
        }
        boolean bl2 = false;
        if (!level.isClientSide) {
            if (level.random.nextInt(5) == 0) {
                level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.plain));
                ItemTreetap.triggerToolUseEvent(level, blockPos, player, blockState, bl);
                bl2 = true;
            }
            if (level.random.nextInt(5) == 0) {
                ItemTreetap.ejectResin(level, blockPos, direction, 1);
                if (list != null) {
                    list.add(new ItemStack((ItemLike)Ic2Items.RESIN));
                } else {
                    ItemTreetap.ejectResin(level, blockPos, direction, 1);
                }
                ItemTreetap.triggerToolUseEvent(level, blockPos, player, blockState, bl);
                bl2 = true;
            }
        }
        return bl2;
    }

    private static void triggerToolUseEvent(Level level, BlockPos blockPos, Player player, BlockState blockState, boolean bl) {
        player.playNotifySound(ItemTreetap.getToolUseSound(bl), SoundSource.PLAYERS, 1.0f, 1.0f);
        level.gameEvent(GameEvent.BLOCK_CHANGE, Vec3.atCenterOf(blockPos), GameEvent.Context.of(player, blockState));
        level.gameEvent(net.minecraft.core.registries.BuiltInRegistries.GAME_EVENT.wrapAsHolder(Ic2GameEvents.TOOL_USE), Vec3.atCenterOf(blockPos), GameEvent.Context.of(player, null));
    }

    private static void ejectResin(Level level, BlockPos blockPos, Direction direction, int n) {
        double d = (double)blockPos.getX() + 0.5 + (double)direction.getStepX() * 0.3;
        double d2 = (double)blockPos.getY() + 0.5 + (double)direction.getStepY() * 0.3;
        double d3 = (double)blockPos.getZ() + 0.5 + (double)direction.getStepZ() * 0.3;
        for (int i = 0; i < n; ++i) {
            ItemEntity itemEntity = new ItemEntity(level, d, d2, d3, new ItemStack((ItemLike)Ic2Items.RESIN));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)itemEntity);
        }
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemStack) {
        return true;
    }

    public static SoundEvent getToolUseSound(boolean bl) {
        return bl ? Ic2SoundEvents.ITEM_TREETAP_ELECTRIC_USE : Ic2SoundEvents.ITEM_TREETAP_USE;
    }
}

