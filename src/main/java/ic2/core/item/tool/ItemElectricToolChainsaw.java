/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Shearable
 *  net.minecraft.world.entity.monster.piglin.PiglinAi
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.gameevent.GameEvent$Context
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.item.tool;

import ic2.api.item.BlockBreakableItem;
import ic2.api.item.IEntityAttackableItem;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.item.tool.ItemElectricTool;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.ref.Ic2ToolMaterials;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.Collections;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ItemElectricToolChainsaw
extends ItemElectricTool
implements IHitSoundOverride,
BlockBreakableItem,
IEntityAttackableItem {
    public ItemElectricToolChainsaw(Item.Properties properties) {
        super(properties, 100, Ic2ToolMaterials.CHAINSAW, Collections.singletonList(BlockTags.MINEABLE_WITH_AXE));
        this.maxCharge = 30000;
        this.transferLimit = 100;
        this.tier = 1;
    }

    private boolean isShearMode(ItemStack itemStack) {
        return !StackUtil.getOrCreateNbtData(itemStack).getBoolean("disableShear");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (level.isClientSide) {
            return super.use(level, player, interactionHand);
        }
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            CompoundTag compoundTag = StackUtil.getOrCreateNbtData(StackUtil.get(player, interactionHand));
            if (compoundTag.getBoolean("disableShear")) {
                compoundTag.putBoolean("disableShear", false);
                IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
            } else {
                compoundTag.putBoolean("disableShear", true);
                IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.noShear");
            }
        }
        return super.use(level, player, interactionHand);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack itemStack, BlockState blockState) {
        return super.isCorrectToolForDrops(itemStack, blockState) || blockState.is(Blocks.COBWEB) || Util.canShear(blockState);
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        return this.canUse(itemStack) && (blockState.is(BlockTags.MINEABLE_WITH_AXE) || blockState.is(Blocks.COBWEB) || Util.canShear(blockState)) ? this.getTier().getSpeed() : 1.0f;
    }

    @Override
    public boolean onAttackEntity(Player player, Entity entity) {
        ItemStack itemStack = player.getMainHandItem();
        if (this.consumeEnergy(itemStack, this.operationEnergyCost, (LivingEntity)player)) {
            this.playUsingSound((LivingEntity)player);
        }
        // 第三十八轮：补回 1.12.2 `hitEntity` 里的成就 —— 用电锯击杀爬行者（`killCreeperChainsaw`）。
        if (entity instanceof Creeper && ((Creeper)entity).getHealth() <= 0.0f) {
            IC2.achievements.issueAchievement(player, "killCreeperChainsaw");
        }
        return true;
    }

    private void handleVanillaBlockBreakLogic(Player player, Level level, BlockPos blockPos, BlockState blockState) {
        level.levelEvent(player, 2001, blockPos, Block.getId((BlockState)blockState));
        if (blockState.is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins((Player)player, (boolean)false);
        }
        level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of((Entity)player, (BlockState)blockState));
    }

    @Override
    public InteractionResult onBlockStartBreak(Player player, Level level, InteractionHand interactionHand, BlockPos blockPos, Direction direction) {
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!this.isShearMode(itemStack) || !Util.canShear(blockState)) {
            return InteractionResult.PASS;
        }
        if (this.consumeEnergy(itemStack, this.operationEnergyCost, (LivingEntity)player)) {
            this.handleVanillaBlockBreakLogic(player, level, blockPos, blockState);
            StackUtil.dropAsEntity(level, blockPos, new ItemStack((ItemLike)blockState.getBlock().asItem()));
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            this.playUsingSound((LivingEntity)player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        if (livingEntity instanceof Shearable) {
            Shearable shearable = (Shearable)livingEntity;
            if (!StackUtil.getOrCreateNbtData(itemStack).getBoolean("disableShear") && this.consumeEnergy(itemStack, this.operationEnergyCost, (LivingEntity)player) && shearable.readyForShearing()) {
                shearable.shear(SoundSource.PLAYERS);
                this.playUsingSound((LivingEntity)player);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public void playUsingSound(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide) {
            return;
        }
        livingEntity.playSound(this.getToolUsingSound(), 1.0f, 1.0f);
    }

    public SoundEvent getToolUsingSound() {
        return IC2.random.nextBoolean() ? Ic2SoundEvents.ITEM_CHAINSAW_USE1 : Ic2SoundEvents.ITEM_CHAINSAW_USE2;
    }

    @Override
    public SoundEvent getHitSoundForBlock(LocalPlayer localPlayer, Level level, BlockPos blockPos, ItemStack itemStack) {
        return this.getToolUsingSound();
    }

    @Override
    public SoundEvent getBreakSoundForBlock(LocalPlayer localPlayer, Level level, BlockPos blockPos, ItemStack itemStack) {
        return null;
    }

    @Override
    protected SoundEvent getIdleSound(LivingEntity livingEntity, ItemStack itemStack) {
        return Ic2SoundEvents.ITEM_CHAINSAW_IDLE;
    }

    @Override
    protected SoundEvent getStopSound(LivingEntity livingEntity, ItemStack itemStack) {
        return Ic2SoundEvents.ITEM_CHAINSAW_STOP;
    }

    @Override
    public boolean beforeBlockBreak(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        return true;
    }

    @Override
    public void afterBlockBreak(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
    }
}

