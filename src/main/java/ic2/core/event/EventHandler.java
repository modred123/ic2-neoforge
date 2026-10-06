/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.monster.Skeleton
 *  net.minecraft.world.entity.monster.Zombie
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.event;

import ic2.api.block.BreakableBlock;
import ic2.api.energy.EnergyNet;
import ic2.api.item.BlockBreakableItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IEntityAttackableItem;
import ic2.api.recipe.Recipes;
import ic2.api.sound.item.ISwingSoundItem;
import ic2.core.ChunkLoaderLogic;
import ic2.core.IC2;
import ic2.core.apihelper.CoreAccessImpl;
import ic2.core.block.ChunkLoadAwareBlockHandler;
import ic2.core.block.comp.Components;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.crop.Ic2Crops;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.event.WorldData;
import ic2.core.init.BlocksItems;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.GatewayElectricItemManager;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.recipe.input.RecipeInputFactory;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2BoatTypes;
import ic2.core.ref.Ic2Entities;
import ic2.core.ref.Ic2GameEvents;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.ref.Ic2RecipeTypes;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.world.Ic2WorldGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class EventHandler {
    public static void onInitGameEvents() {
        Ic2SoundEvents.init();
        Ic2GameEvents.init();
    }

    public static void onInitEarly() {
        long l = System.nanoTime();
        IC2.log.debug(LogCategory.General, "Starting pre-init.");
        MainConfig.load();
        CoreAccessImpl.init();
        IC2.sideProxy.getAudioManager().initialize();
        Recipes.inputFactory = new RecipeInputFactory();
        EnergyNet.instance = EnergyNetGlobal.create();
        ElectricItem.manager = new GatewayElectricItemManager();
        ElectricItem.rawManager = new ElectricItemManager();
        Components.init();
        BlocksItems.init();
        Ic2BlockTags.init();
        Ic2ItemTags.init();
        Ic2BoatTypes.init();
        Ic2Entities.init();
        Ic2RecipeTypes.init();
        Ic2RecipeSerializers.init();
        Ic2WorldGen.init();
        TileEntityRecycler.init();
        TileEntityMatter.init();
        TileEntitySemifluidGenerator.init();
        TileEntityFluidHeatGenerator.init();
        TileEntityBlockCutter.init();
        TileEntityLiquidHeatExchanger.init();
        TileEntityFermenter.init();
        TileEntityElectrolyzer.init();
        Rezepte.registerRecipes();
        Ic2Crops.init();
        IC2.sideProxy.preInit();
        IC2.initialized = true;
        IC2.log.debug(LogCategory.General, "Finished pre-init after %d ms.", (System.nanoTime() - l) / 1000000L);
    }

    public static void onInit() {
    }

    public static void onInitLate() {
        long l = System.nanoTime();
        IC2.sideProxy.onPostInit();
        // 1.21 迁移修复（第十六轮）：onInitLate 在 FMLLoadCompleteEvent 派发时执行，此时玩家通常
        // 还在标题界面、integrated server 尚未创建——上一轮"用 getSingleplayerServer()!=null 判定
        // server tick"的写法在加载期恒为 false，任务被排到 client 线程执行，语义仍是错的；且加载
        // 完成即结束、client 无后续调度需求，init 映射表虽建成但后续 tick 链（本轮已由构造器注册
        // 游戏总线恢复）对线程归属仍有要求（ChunkLoadAwareBlockHandler 的 onLoad/onUnload 在 server
        // tick 中回调）。修复：恒定传 serverTick=true，由 SideProxy 各端自行路由——
        //   dedicated server：SideProxyServer.requestTick(true) 缓冲到 ServerStartingEvent 后执行；
        //   client（含单机）：SideProxyClient.requestTick(true) 先挂 client 线程，开服后自动转投
        //   integratedServer.execute。彻底消除对"调度时刻 server 是否已存在"的时序依赖。
        IC2.sideProxy.requestTick(true, (Runnable)ChunkLoadAwareBlockHandler::init);
        IC2.log.debug(LogCategory.General, "Finished post-init after %d ms.", (System.nanoTime() - l) / 1000000L);
        IC2.log.info(LogCategory.General, "%s version %s loaded.", "ic2", "2.9.162+ex119");
    }

    private static boolean loadSubModule(String string) {
        IC2.log.debug(LogCategory.SubModule, "Loading %s submodule: %s.", "ic2", string);
        try {
            Class<?> clazz = IC2.class.getClassLoader().loadClass("ic2." + string + ".SubModule");
            return (Boolean)clazz.getMethod("init").invoke(null);
        }
        catch (Throwable throwable) {
            IC2.log.debug(LogCategory.SubModule, "Submodule %s not loaded.", string);
            return false;
        }
    }

    public static void onServerStart(MinecraftServer minecraftServer) {
        IC2.sideProxy.onServerAvailable(minecraftServer);
    }

    public static void onPlayerLogout(Player player) {
        if (IC2.sideProxy.isSimulating()) {
            IC2.sideProxy.getKeyboard().removePlayerReferences(player);
        }
    }

    public static void onWorldLoad(Level level) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel)level;
            ChunkLoaderLogic.onWorldLoad(serverLevel);
        }
    }

    public static void onWorldUnload(Level level) {
        WorldData.onWorldUnload(level);
    }

    public static void onChunkDataLoad(LevelChunk levelChunk, CompoundTag compoundTag) {
    }

    public static void onChunkSave(LevelChunk levelChunk, CompoundTag compoundTag) {
    }

    public static void onChunkLoad(LevelChunk levelChunk) {
        ChunkLoadAwareBlockHandler.onChunkLoad(levelChunk);
    }

    public static void onChunkUnload(LevelChunk levelChunk) {
        ChunkLoadAwareBlockHandler.onChunkUnload(levelChunk);
        if (!levelChunk.getLevel().isClientSide) {
            ChunkLoaderLogic.onChunkUnload(levelChunk);
        }
    }

    public static InteractionResult onBlockStartBreak(Player player, Level level, InteractionHand interactionHand, BlockPos blockPos, Direction direction) {
        BlockBreakableItem blockBreakableItem;
        Item item = player.getItemInHand(interactionHand).getItem();
        if (item instanceof BlockBreakableItem) {
            InteractionResult result = (blockBreakableItem = (BlockBreakableItem)item).onBlockStartBreak(player, level, interactionHand, blockPos, direction);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof BreakableBlock) {
            BreakableBlock breakableBlock = (BreakableBlock)block;
            return breakableBlock.startBreak(player, level, interactionHand, blockPos, blockState, direction);
        }
        return InteractionResult.PASS;
    }

    public static boolean beforeBlockBreak(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        Item item = player.getMainHandItem().getItem();
        if (item instanceof BlockBreakableItem) {
            return ((BlockBreakableItem)item).beforeBlockBreak(level, player, blockPos, blockState, blockEntity);
        }
        return true;
    }

    public static void afterBlockBreak(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        Item item = player.getMainHandItem().getItem();
        if (item instanceof BlockBreakableItem) {
            ((BlockBreakableItem)item).afterBlockBreak(level, player, blockPos, blockState, blockEntity);
        }
    }

    public static void onPlayerTick(Player player) {
    }

    public static void onLivingSpecialSpawn(LivingEntity livingEntity) {
        if (IC2.seasonal && (livingEntity instanceof Zombie || livingEntity instanceof Skeleton) && livingEntity.getCommandSenderWorld().random.nextFloat() < 0.1f) {
            Mob mob = (Mob)livingEntity;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                mob.setDropChance(equipmentSlot, Float.NEGATIVE_INFINITY);
            }
        }
    }

    public static boolean onLivingFall(LivingEntity livingEntity, float f) {
        if (livingEntity.getCommandSenderWorld().isClientSide) {
            return false;
        }
        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
        if (StackUtil.isEmpty(itemStack)) {
            return false;
        }
        Item item = itemStack.getItem();
        if (item == Ic2Items.RUBBER_BOOTS) {
            return ((ItemArmorHazmat)item).absorbFall(itemStack, livingEntity, f);
        }
        if (item == Ic2Items.NANO_BOOTS) {
            return ((ItemArmorNanoSuit)item).absorbFall(itemStack, livingEntity, f);
        }
        if (item == Ic2Items.QUANTUM_BOOTS) {
            return ((ItemArmorQuantumSuit)item).absorbFall(itemStack, livingEntity, f);
        }
        return false;
    }

    public static boolean onEntitySwingHand(LivingEntity livingEntity, InteractionHand interactionHand) {
        ItemStack itemStack = livingEntity.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (!(item instanceof ISwingSoundItem)) {
            return false;
        }
        ISwingSoundItem iSwingSoundItem = (ISwingSoundItem)item;
        SoundEvent swingSound = iSwingSoundItem.getSwingSound(livingEntity, interactionHand);
        if (swingSound != null) {
            livingEntity.playSound(swingSound, 1.0f, 1.0f);
        }
        return false;
    }

    public static boolean onEntityInteract(Player player, InteractionHand interactionHand, Entity entity) {
        if (player.getCommandSenderWorld().isClientSide) {
            return false;
        }
        ItemStack itemStack = StackUtil.get(player, interactionHand);
        if (StackUtil.isEmpty(itemStack)) {
            return false;
        }
        Item item = itemStack.getItem();
        return false;
    }

    public static boolean onAttackEntity(Player player, Entity entity) {
        Item item = player.getMainHandItem().getItem();
        if (item instanceof IEntityAttackableItem) {
            return ((IEntityAttackableItem)item).onAttackEntity(player, entity);
        }
        return true;
    }

    public static boolean onEntityAttacked(LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (!(livingEntity instanceof Player)) {
            return true;
        }
        ItemArmorElectric.damageArmor((Player)livingEntity, damageSource, f);
        return true;
    }
}

