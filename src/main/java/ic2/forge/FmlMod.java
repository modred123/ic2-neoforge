/*
 * Decompiled with CFR 0.152.
 */
package ic2.forge;

import ic2.core.event.EventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.fluid.FluidBeBridge;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidItem;
import ic2.forge.BlockFluidCapImpl;
import ic2.forge.ItemFluidCapImpl;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(value="ic2")
public final class FmlMod {
    private List<Runnable> toRunAfterRegistryInit = new ArrayList<Runnable>();
    public static FmlMod instance;
    private static final AtomicInteger loadState;

    public FmlMod(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        modEventBus.register(this);
        // 手动注册 FmlCommonSetupEvent 监听器（避免 javafml 注解扫描对 fml 事件可能不可靠）：
        // EventPriority.LOWEST 让 ic2 内部的 modBus 监听器排最晚，期望能晚于大部分 mod 的 setup 处理器。
        modEventBus.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
            FMLCommonSetupEvent.class, this::onCommonSetup);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.register(new ClientModEventHandlerForge());
        }
        // 1.21 迁移修复（第二十一轮）：原写法 `addListener(ForgeNetworkHandler::register)` 只有
        // 一个参数，走的是 EventBus 的"泛型推断"重载 —— 该重载需要 typetools 从运行期生成的
        // lambda 隐藏类里反推事件类型，对方法引用存在解析退化风险（解析不出则监听器静默失效）。
        // 现改为**显式传入事件 Class** 的重载（不走泛型推断，与 modEventBus.register(this) 等效可靠），
        // 并在 FmlMod 内另加 @SubscribeEvent 兜底（见 onRegisterPayloads），二者由
        // ForgeNetworkHandler 内部的幂等开关保证只生效一次。
        modEventBus.addListener(RegisterPayloadHandlersEvent.class,
            (RegisterPayloadHandlersEvent event) -> ForgeNetworkHandler.register(event, "addListener-explicitClass"));
        // 1.21 迁移修复（第十六轮）：原版 Forge 在 load(FMLCommonSetupEvent) 里向游戏总线注册
        // EventHandlerForge/ClientEventHandlerForge，但 NeoForge 21.1 对 ic2 的 FMLCommonSetupEvent
        // 派发不可靠（实测 loadState 恒为 1，load() 从不执行）→ 游戏总线监听器从未注册 →
        // LevelTickEvent/ChunkEvent.Load/LevelEvent.Load/EnergyTileLoadEvent 等全部无人处理 →
        // EnergyNet 不 tick（太阳能无阳光、电缆不连机器）、TeUpdate 网络同步停摆（GUI 按钮无反应）、
        // 区块加载时 TE 不入能量网。改为在 mod 构造器直接注册到 NeoForge.EVENT_BUS：
        // mod 构造早于一切游戏事件，注册时机绝对可靠，且无需依赖 FML 生命周期事件派发。
        NeoForge.EVENT_BUS.register(new EventHandlerForge());
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.register(new ClientEventHandlerForge());
        }
        // 提前加载配方注册类：确保 Ic2RecipeTypes/Ic2RecipeSerializers 的静态字段
        // （RecipeType/RecipeSerializer 条目缓冲）在任何 RegisterEvent 触发之前完成，
        // 否则 RECIPE_SERIALIZER/RECIPE_TYPE 的 RegisterEvent 可能先于 onInitEarly 执行，
        // 导致 serializer/type 永远注册不上 → 配方 JSON 无法解析 → JEI 无配方。
        ic2.core.ref.Ic2RecipeTypes.init();
        ic2.core.ref.Ic2RecipeSerializers.init();
        // 实体注册：已改为 onInitEarly（Ic2Entities.init()）统一缓冲 + RegisterEvent flush
        // （按 neoforge-api-spec.md 3.3 统一缓冲模式，删除 DeferredRegister 方式）
    }

    /**
     * FMLCommonSetupEvent 处理器：dump + 暴力兜底（手动 addListener 注册，绕过 javafml @SubscribeEvent）。
     * 失败时静默（FmlMod 构造器也调用了 dumpBoundState 作为兜底诊断）。
     */
    private void onCommonSetup(FMLCommonSetupEvent event) {
        try {
            ic2.core.ref.Ic2Entities.dumpUnregisteredEntityTypesWithFallback();
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").error(
                "FMLCommonSetup dump failed: {}", t.toString(), t);
        }
    }

    /**
     * 注意：原 @SubscribeEvent(priority=EventPriority.LOWEST) 注解方式对 FMLCommonSetupEvent
     * 似乎未触发（latest.log 无 dump 输出）—— 改为构造器中手动 addListener 注册。
     * 留此方法保留以备 javafml 行为变化时回退，并加一行 ERROR 日志以便确认派发状态。
     */
    public void load(FMLCommonSetupEvent fMLCommonSetupEvent) {
        org.apache.logging.log4j.LogManager.getLogger("ic2-diag").error(
            "FmlMod.load (legacy @SubscribeEvent) entered — should not happen if addListener works");
        ic2.core.ref.Ic2Entities.dumpUnregisteredEntityTypesWithFallback();
        // 注意：EventHandlerForge / ClientEventHandlerForge 已改到构造器注册（见构造器注释），
        // 此处不再注册，避免万一 FML 事件恢复派发时造成游戏总线监听器重复注册（事件双次处理）。
        if (!loadState.compareAndSet(1, 2)) {
            throw new IllegalStateException();
        }
        EventHandler.onInit();
    }

    /**
     * 1.21 迁移兜底（第二十一轮）：payload 注册的第二条路径。
     * 本类实例已由 modEventBus.register(this)（构造器第 47 行）注册，@SubscribeEvent 方法
     * 的类型信息来自方法**参数类型**（反射可得，不涉及 lambda 泛型擦除），因此这条路径
     * 与 registerBlocks(RegisterEvent) 一样可靠 —— 后者已被实测证明正常工作。
     * 与构造器里的 addListener(显式Class) 互为备份，由幂等开关保证只生效一次。
     */
    @SubscribeEvent
    public void onRegisterPayloads(RegisterPayloadHandlersEvent registerPayloadHandlersEvent) {
        ForgeNetworkHandler.register(registerPayloadHandlersEvent, "subscribeEvent");
    }

    @SubscribeEvent
    public void init(FMLLoadCompleteEvent fMLLoadCompleteEvent) {
        // 兜底 dump：FMLLoadCompleteEvent 在 COMPLETION 阶段派发，若 freezeData 抛异常 NeoForge 仍会派发它
        try {
            ic2.core.ref.Ic2Entities.dumpUnregisteredEntityTypes();
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").error(
                "FMLLoadComplete dump failed: {}", t.toString());
        }
        if (!loadState.compareAndSet(2, 3)) {
            // loadState 不是 2，常见原因：FMLCommonSetupEvent 在 NeoForge 21.1 对 ic2 派发不可靠，
            // 导致 FmlMod.load 的 1→2 转换未发生。改为 log warn 而非 throw ISE，避免阻断游戏加载。
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").warn(
                "FmlMod.init: loadState={} (expected 2); continuing onInitLate", loadState.get());
        }
        EventHandler.onInitLate();
    }

    @SubscribeEvent
    public void registerBlocks(RegisterEvent registerEvent) {
        // 在第一个 RegisterEvent 触发一次性初始化：创建所有对象并缓冲注册
        if (loadState.compareAndSet(0, 1)) {
            // 1.21 迁移修复：IC2.<clinit> 调 EnergyNetGlobal.create() 时 Minecraft instance 尚未创建。
            // 历史沿革：第十四轮曾按 isClientEnv() 回退到 no-op stub（EnergyNetClient），但 create() 只在
            // mod 加载期调用，那时 integrated server 必然不存在 → 单机恒拿 stub → 能量网被静默吞掉
            // （太阳能无阳光检测、电缆不连机器）。第十八轮已撤销该分支：create() 恒返真实
            // EnergyNetGlobal + EnergyCalculatorLeg，客户端安全改由调用点 !isClientSide 守卫保证
            // （见 AbstractCableBlock.addToEnet/removeFromEnet）。
            // 此处仍显式重设一次 EnergyNet.instance，确保 Minecraft 就绪后单机拿到真实实现。
            // 注：EnergyNetClient 已于 2026-09-23 作为死代码移除（归档于
            // neoforge_api_analysis/dead-code-archive/EnergyNetClient.java）。
            ic2.api.energy.EnergyNet.instance = ic2.core.energy.grid.EnergyNetGlobal.create();
            EventHandler.onInitEarly();
            EventHandler.onInitGameEvents();
        }
        // 逐个注册表 flush 缓冲
        int before = EnvProxyForge.pendingRegistrationsSize();
        EnvProxyForge.flushPendingRegistrations(registerEvent);
        int after = EnvProxyForge.pendingRegistrationsSize();
        if (before != after) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").info(
                "flushed {} entries for {} (remaining {})", before - after, registerEvent.getRegistryKey(), after);
        } else {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").debug(
                "no flush for {} (pending {})", registerEvent.getRegistryKey(), before);
        }
        // 尝试创建依赖 EntityType 的 BoatItem 三个物品。
        // 创建检查 isBound() 而非 .get()——后者会抛 NPE；isBound() 返回 false 表示 EntityType 还没绑定（ENTITY_TYPE RegisterEvent 尚未触发）。
        // 每次 RegisterEvent 触发都尝试，确保 ITEM RegisterEvent 之前（不论是哪个先触发）都能被及时创建。
        if (registerEvent.getRegistryKey() == Registries.ENTITY_TYPE
                || registerEvent.getRegistryKey() == Registries.ITEM) {
            ic2.core.ref.Ic2Items.tryCreateBoatItems();
        }
    }


    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<?> beType : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, beType, (be, side) -> {
                if (be instanceof Ic2TileEntity && be instanceof FluidBeBridge bridge) {
                    Ic2FluidBlock fluidBlock = bridge.getFluidBlock();
                    if (fluidBlock != null && fluidBlock.isFluidBlock(null, null, null, be)) {
                        return new BlockFluidCapImpl(fluidBlock, be).getHandler(side);
                    }
                }
                return null;
            });
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, beType, (be, side) -> {
                if (be instanceof WorldlyContainer wc) {
                    return new SidedInvWrapper(wc, side);
                } else if (be instanceof Container c) {
                    return new InvWrapper(c);
                }
                return null;
            });
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof Ic2FluidItem) {
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, side) -> new ItemFluidCapImpl(stack), item);
            }
            // ex112 兼容：ItemIC2FluidContainer 体系（流体容器物品，1.12.2 addCapability 机制迁移）
            if (item instanceof ic2.core.item.ItemIC2FluidContainer fluidContainer) {
                fluidContainer.registerFluidCapabilities(event);
            }
        }
    }

    @SubscribeEvent
    public void registerLate(RegisterEvent registerEvent) {
        if (registerEvent.getRegistryKey() != NeoForgeRegistries.Keys.HOLDER_SET_TYPES) {
            return;
        }
        // 所有注册表的 RegisterEvent 均已派发完毕（HOLDER_SET_TYPES 是最后一个）：
        // 1) 兜底注册：若 DeferredRegister 漏注册了实体（intrusive holder 泄漏会导致
        //    freeze 时报 "Some intrusive holders were not registered"），用缓存的同一实例补注册；
        // 2) 打印 7 个实体 holder 的绑定状态，供排查。
        ic2.core.ref.Ic2Entities.ensureAllRegistered();
        ic2.core.ref.Ic2Entities.dumpBoundState();
        for (Runnable runnable : this.toRunAfterRegistryInit) {
            runnable.run();
        }
        this.toRunAfterRegistryInit = null;
    }

    void runAfterRegistryInit(Runnable runnable) {
        if (loadState.get() > 1) {
            runnable.run();
        } else {
            this.toRunAfterRegistryInit.add(runnable);
        }
    }

    static {
        loadState = new AtomicInteger();
    }
}
