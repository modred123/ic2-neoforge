/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.CriteriaTriggers
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 */
package ic2.core.util;

import java.util.function.Consumer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class WorldUtil {
    public static void findTileEntities(Level level, BlockPos blockPos, int n, ITileEntityResultHandler iTileEntityResultHandler) {
        int n2 = blockPos.getX() - n;
        int n3 = blockPos.getY() - n;
        int n4 = blockPos.getZ() - n;
        int n5 = blockPos.getX() + n;
        int n6 = blockPos.getY() + n;
        int n7 = blockPos.getZ() + n;
        int n8 = n2 >> 4;
        int n9 = n4 >> 4;
        int n10 = n5 >> 4;
        int n11 = n7 >> 4;
        for (int i = n8; i <= n10; ++i) {
            for (int j = n9; j <= n11; ++j) {
                LevelChunk levelChunk = level.getChunk(i, j);
                for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
                    BlockPos blockPos2 = blockEntity.getBlockPos();
                    if (blockPos2.getY() < n3 || blockPos2.getY() > n6 || blockPos2.getX() < n2 || blockPos2.getX() > n5 || blockPos2.getZ() < n4 || blockPos2.getZ() > n7 || !iTileEntityResultHandler.onMatch(blockEntity)) continue;
                    return;
                }
            }
        }
    }

    public static void strip(BlockState blockState, Level level, BlockPos blockPos, Player player, ItemStack itemStack, BlockState blockState2) {
        level.playSound(player, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
        }
        level.setBlock(blockPos, blockState2, 11);
        itemStack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    public static interface ITileEntityResultHandler {
        public boolean onMatch(BlockEntity var1);
    }
}

