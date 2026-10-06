/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.WorldlyContainer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.neoforged.neoforge.capabilities.Capability
 *  net.neoforged.neoforge.capabilities.ForgeCapabilities
 *  net.neoforged.neoforge.capabilities.ICapabilityProvider
 *  net.neoforged.neoforge.common.util.LazyOptional
 *  net.neoforged.neoforge.common.util.NonNullSupplier
 *  net.neoforged.neoforge.event.AttachCapabilitiesEvent
 *  net.neoforged.neoforge.event.TickEvent$LevelTickEvent
 *  net.neoforged.neoforge.event.TickEvent$Phase
 *  net.neoforged.neoforge.event.TickEvent$PlayerTickEvent
 *  net.neoforged.neoforge.event.TickEvent$ServerTickEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.entity.living.LivingFallEvent
 *  net.neoforged.neoforge.event.entity.living.LivingSpawnEvent$SpecialSpawn
 *  net.neoforged.neoforge.event.entity.player.AttackEntityEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickEmpty
 *  net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 *  net.neoforged.neoforge.event.level.ChunkDataEvent$Load
 *  net.neoforged.neoforge.event.level.ChunkDataEvent$Save
 *  net.neoforged.neoforge.event.level.ChunkEvent$Load
 *  net.neoforged.neoforge.event.level.ChunkEvent$Unload
 *  net.neoforged.neoforge.event.level.LevelEvent$Load
 *  net.neoforged.neoforge.event.level.LevelEvent$Unload
 *  net.neoforged.neoforge.event.server.ServerStartingEvent
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.neoforge.items.IItemHandler
 *  net.neoforged.neoforge.items.IItemHandlerModifiable
 *  net.neoforged.neoforge.items.wrapper.InvWrapper
 *  net.neoforged.neoforge.items.wrapper.SidedInvWrapper
 */
package ic2.forge;

import net.minecraft.core.registries.Registries;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.event.RetextureEvent;
import ic2.api.tile.RetexturableBlock;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.event.EventHandler;
import ic2.core.event.TickHandler;
import ic2.core.fluid.FluidBeBridge;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.init.OreValues;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import ic2.forge.BlockFluidCapImpl;
import ic2.forge.EnvProxyForge;
import ic2.forge.ItemFluidCapImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;



import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class EventHandlerForge {
    private static final ResourceLocation fluidCapId = IC2.getIdentifier("fluid");
    private static final ResourceLocation itemCapId = IC2.getIdentifier("item");

    @SubscribeEvent
    public void serverStart(ServerStartingEvent serverStartingEvent) {
        EventHandler.onServerStart(serverStartingEvent.getServer());
    }

    // 第三十六轮：数据包/标签重载后丢弃矿石判定缓存 —— OreValues 现在按方块标签判定"是不是矿石"
    // （1.12.2 的 OreDictionary 等价物，见 OreValues 类注释）。本事件客户端/服务端都会派发，
    // 而处理器只是把缓存置空、下次判定按最新标签重建，双端调用均无副作用。
    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent tagsUpdatedEvent) {
        OreValues.invalidateOreTags();
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        EventHandler.onPlayerLogout(playerLoggedOutEvent.getEntity());
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load load) {
        EventHandler.onWorldLoad((Level)load.getLevel());
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload unload) {
        EventHandler.onWorldUnload((Level)unload.getLevel());
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load load) {
        ChunkAccess chunkAccess = load.getChunk();
        if (chunkAccess instanceof LevelChunk levelChunk) {
            EventHandler.onChunkDataLoad(levelChunk, load.getData());
            // 1.21 迁移修复（第十六轮）：原版 IC2 的 ChunkEvent.Load 处理器没有 @SubscribeEvent
            // （原版 jar 字节码实证，CFR 无遗漏）且 Forge 时代其语义为服务端专属；而 NeoForge
            // 1.21.1 的 ChunkEvent 双端派发（客户端 ModelDataManager 亦订阅）——电缆/能量方块
            // onLoad→addToEnet 只能在服务端跑（EnergyNetGlobal.getLocal 对客户端抛异常）。
            // 故把 ChunkLoadAwareBlockHandler.onChunkLoad 挂到服务端专属的 ChunkDataEvent.Load
            // （区块 NBT 加载完成，BE 已入 pending、电缆加载已发生，Grid 更新在 tick 末尾统一
            // 处理，无时序问题），恢复"已放置电缆/能量方块重新入网"能力——电缆无法连接机器的
            // 直接修复点。
            EventHandler.onChunkLoad(levelChunk);
        }
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save save) {
        ChunkAccess chunkAccess = save.getChunk();
        if (chunkAccess instanceof LevelChunk) {
            EventHandler.onChunkSave((LevelChunk)chunkAccess, save.getData());
        }
    }

    // 1.21 迁移说明：本方法在原版中就没有 @SubscribeEvent（CFR 反编译无遗漏）——ChunkEvent.Load
    // 双端派发，而其下游是服务端专属逻辑（能量网注册会因客户端调用 EnergyNetGlobal.getLocal 抛
    // "not applicable clientside"），原版刻意不激活它，改由服务端专属的 ChunkDataEvent.Load 承担
    // 入网（见 onChunkDataLoad）。保留本方法仅作诊断，勿加注解。
    public void onChunkLoad(ChunkEvent.Load load) {
        ChunkAccess chunkAccess = load.getChunk();
        if (chunkAccess instanceof LevelChunk levelChunk && !levelChunk.getLevel().isClientSide) {
            EventHandler.onChunkLoad((LevelChunk)chunkAccess);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onChunkUnload(ChunkEvent.Unload unload) {
        ChunkAccess chunkAccess = unload.getChunk();
        // 1.21 迁移修复：同 onChunkLoad——NeoForge 1.21.1 的 ChunkEvent 双端派发，而下游
        // （removeFromEnet / ChunkLoaderLogic）只允许服务端线程调用，客户端调用会触发
        // EnergyNetGlobal.getLocal 的 "not applicable clientside" 断言/异常或并发问题。加服务端守卫。
        if (chunkAccess instanceof LevelChunk levelChunk && !levelChunk.getLevel().isClientSide) {
            EventHandler.onChunkUnload((LevelChunk)chunkAccess);
        }
    }

    @SubscribeEvent
    public void onWorldTickStart(LevelTickEvent.Pre event) {
        TickHandler.onWorldTickStart(event.getLevel());
    }

    @SubscribeEvent
    public void onWorldTickEnd(LevelTickEvent.Post event) {
        TickHandler.onWorldTickEnd(event.getLevel());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre serverTickEvent) {
        TickHandler.onServerTick();
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Pre playerTickEvent) {
        EventHandler.onPlayerTick(playerTickEvent.getEntity());
    }

    // TODO: SpecialSpawn 事件在 NeoForge 1.21.1 已移除，需确认替代方案
    /*
    @SubscribeEvent
    public void onLivingSpecialSpawn(MobSpawnEvent.SpawnPlacementCheck specialSpawn) {
        EventHandler.onLivingSpecialSpawn((LivingEntity)specialSpawn.getEntity());
    }
    */

    @SubscribeEvent(priority=EventPriority.LOW)
    public void onLivingFall(LivingFallEvent livingFallEvent) {
        if (EventHandler.onLivingFall(livingFallEvent.getEntity(), livingFallEvent.getDistance())) {
            livingFallEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty leftClickEmpty) {
        EventHandler.onEntitySwingHand((LivingEntity)leftClickEmpty.getEntity(), leftClickEmpty.getHand());
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock leftClickBlock) {
        if (EventHandler.onEntitySwingHand((LivingEntity)leftClickBlock.getEntity(), leftClickBlock.getHand())) {
            leftClickBlock.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract entityInteract) {
        if (EventHandler.onEntityInteract(entityInteract.getEntity(), entityInteract.getHand(), entityInteract.getTarget())) {
            entityInteract.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public void onEntityAttacked(LivingIncomingDamageEvent livingAttackEvent) {
        LivingEntity livingEntity = livingAttackEvent.getEntity();
        if (!EventHandler.onEntityAttacked(livingEntity, livingAttackEvent.getSource(), livingAttackEvent.getAmount())) {
            livingAttackEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public void onAttackEntity(AttackEntityEvent attackEntityEvent) {
        if (!EventHandler.onAttackEntity(attackEntityEvent.getEntity(), attackEntityEvent.getTarget())) {
            attackEntityEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockStartBreak(PlayerInteractEvent.LeftClickBlock leftClickBlock) {
        if (EventHandler.onBlockStartBreak(leftClickBlock.getEntity(), leftClickBlock.getLevel(), leftClickBlock.getHand(), leftClickBlock.getPos(), leftClickBlock.getFace()) == InteractionResult.FAIL) {
            leftClickBlock.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void beforeBlockBreak(BlockEvent.BreakEvent breakEvent) {
        Level level = (Level)breakEvent.getLevel();
        BlockPos blockPos = breakEvent.getPos();
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!EventHandler.beforeBlockBreak(level, breakEvent.getPlayer(), blockPos, breakEvent.getState(), blockEntity)) {
            breakEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGetBurnTime(FurnaceFuelBurnTimeEvent furnaceFuelBurnTimeEvent) {
        Item item = furnaceFuelBurnTimeEvent.getItemStack().getItem();
        if (EnvProxyForge.burnTimeRecord.containsKey(item)) {
            furnaceFuelBurnTimeEvent.setBurnTime(EnvProxyForge.burnTimeRecord.get(item).intValue());
        }
    }

    @SubscribeEvent
    public void onRetexture(RetextureEvent retextureEvent) {
        Block block = retextureEvent.state.getBlock();
        if (block instanceof RetexturableBlock && ((RetexturableBlock)block).retexture(retextureEvent.state, (Level)retextureEvent.getLevel(), retextureEvent.pos, retextureEvent.side, retextureEvent.player, retextureEvent.refState, retextureEvent.refVariant, retextureEvent.refSide, retextureEvent.refColorMultipliers)) {
            retextureEvent.applied = true;
            retextureEvent.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEnergyTileLoad(EnergyTileLoadEvent energyTileLoadEvent) {
        if (energyTileLoadEvent.getLevel().isClientSide()) {
            IC2.log.warn(LogCategory.EnergyNet, "EnergyTileLoadEvent: posted for %s client-side, aborting", Util.toString(energyTileLoadEvent.tile, (BlockGetter)energyTileLoadEvent.getLevel(), EnergyNet.instance.getPos(energyTileLoadEvent.tile)));
            return;
        }
        EnergyNet.instance.addTileUnchecked(energyTileLoadEvent.tile);
    }

    @SubscribeEvent
    public void onEnergyTileUnload(EnergyTileUnloadEvent energyTileUnloadEvent) {
        if (energyTileUnloadEvent.getLevel().isClientSide()) {
            IC2.log.warn(LogCategory.EnergyNet, "EnergyTileUnloadEvent: posted for %s client-side, aborting", Util.toString(energyTileUnloadEvent.tile, (BlockGetter)energyTileUnloadEvent.getLevel(), EnergyNet.instance.getPos(energyTileUnloadEvent.tile)));
            return;
        }
        EnergyNet.instance.removeTile(energyTileUnloadEvent.tile);
    }


}

