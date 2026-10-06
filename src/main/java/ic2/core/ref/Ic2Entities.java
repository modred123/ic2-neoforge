/*
 * IC2 ex119 mod — NeoForge 1.21.1 迁移修复版
 *
 * 按 neoforge-api-spec.md 3.3「统一缓冲模式」整改：
 *   - 删除 DeferredRegister（与 IC2"第一个 RegisterEvent 一次性 onInitEarly"架构冲突，
 *     叶子注册表（ENTITY_TYPE 等）在 BLOCK 之前触发，DeferredRegister 时序不可控）；
 *   - 改为：EntityType 在 onInitEarly（Ic2Entities.init()）时 build，
 *     经 EnvProxyForge.registerEntity 缓冲，ENTITY_TYPE RegisterEvent 时统一 flush 注册；
 *   - 字段类型 EntityType<T>（非 final），init() 前为 null，运行时保证非 null。
 */
package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.entity.LaserBulletEntity;
import ic2.core.entity.block.ITntEntity;
import ic2.core.entity.block.NukeEntity;
import ic2.core.entity.boat.CarbonBoatEntity;
import ic2.core.entity.boat.ElectricBoatEntity;
import ic2.core.entity.boat.RubberBoatEntity;
import ic2.core.item.tool.EntityParticle;
import ic2.forge.EnvProxyForge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;

public final class Ic2Entities {
    // EntityType 实例：onInitEarly（Ic2Entities.init()）时构建并缓冲注册；init() 前为 null
    public static EntityType<ITntEntity> ITNT;
    public static EntityType<NukeEntity> NUKE;
    public static EntityType<LaserBulletEntity> LASER_BULLET;
    public static EntityType<EntityParticle> PLASMA_PARTICLE;
    public static EntityType<RubberBoatEntity> RUBBER_BOAT;
    public static EntityType<ElectricBoatEntity> ELECTRIC_BOAT;
    public static EntityType<CarbonBoatEntity> CARBON_BOAT;

    /** 由 EventHandler.onInitEarly（第一个 RegisterEvent）调用：build 全部实体并缓冲注册 */
    public static void init() {
        ITNT = EntityType.Builder.<ITntEntity>of(ITntEntity::new, MobCategory.MISC)
            .fireImmune().sized(0.98f, 0.98f).clientTrackingRange(10).updateInterval(10)
            .build("itnt");
        IC2.envProxy.registerEntity(IC2.getIdentifier("itnt"), ITNT);

        NUKE = EntityType.Builder.<NukeEntity>of(NukeEntity::new, MobCategory.MISC)
            .fireImmune().sized(0.98f, 0.98f).clientTrackingRange(10).updateInterval(10)
            .build("nuke");
        IC2.envProxy.registerEntity(IC2.getIdentifier("nuke"), NUKE);

        LASER_BULLET = EntityType.Builder.<LaserBulletEntity>of(LaserBulletEntity::new, MobCategory.MISC)
            .fireImmune().sized(0.8f, 0.8f).clientTrackingRange(8).updateInterval(8)
            .build("laser_bullet");
        IC2.envProxy.registerEntity(IC2.getIdentifier("laser_bullet"), LASER_BULLET);

        PLASMA_PARTICLE = EntityType.Builder.<EntityParticle>of(EntityParticle::new, MobCategory.MISC)
            .fireImmune().sized(0.2f, 0.2f).clientTrackingRange(8).updateInterval(2)
            .build("plasma_particle");
        IC2.envProxy.registerEntity(IC2.getIdentifier("plasma_particle"), PLASMA_PARTICLE);

        RUBBER_BOAT = EntityType.Builder.<RubberBoatEntity>of(RubberBoatEntity::new, MobCategory.MISC)
            .sized(1.375f, 0.5625f).clientTrackingRange(10).build("rubber_boat");
        IC2.envProxy.registerEntity(IC2.getIdentifier("rubber_boat"), RUBBER_BOAT);

        ELECTRIC_BOAT = EntityType.Builder.<ElectricBoatEntity>of(ElectricBoatEntity::new, MobCategory.MISC)
            .sized(1.375f, 0.5625f).clientTrackingRange(10).build("electric_boat");
        IC2.envProxy.registerEntity(IC2.getIdentifier("electric_boat"), ELECTRIC_BOAT);

        CARBON_BOAT = EntityType.Builder.<CarbonBoatEntity>of(CarbonBoatEntity::new, MobCategory.MISC)
            .sized(1.375f, 0.5625f).clientTrackingRange(10).build("carbon_boat");
        IC2.envProxy.registerEntity(IC2.getIdentifier("carbon_boat"), CARBON_BOAT);
    }

    /** FmlMod 构造器调用占位：实体注册已由 init() 缓冲 + flush 处理，无需额外动作 */
    public static void register(IEventBus modBus) {
    }

    /**
     * 兜底注册 + 诊断（由 FmlMod.registerLate 在最后一个注册表 RegisterEvent 时调用）。
     * 若 EntityType 已 build 但未注册（intrusive holder 泄漏，freeze 时报
     * "Some intrusive holders were not registered"），用同一实例补注册绑定 holder。
     */
    public static void ensureAllRegistered() {
        registerIfUnbound("itnt", ITNT);
        registerIfUnbound("nuke", NUKE);
        registerIfUnbound("laser_bullet", LASER_BULLET);
        registerIfUnbound("plasma_particle", PLASMA_PARTICLE);
        registerIfUnbound("rubber_boat", RUBBER_BOAT);
        registerIfUnbound("electric_boat", ELECTRIC_BOAT);
        registerIfUnbound("carbon_boat", CARBON_BOAT);
    }

    private static void registerIfUnbound(String id, EntityType<?> type) {
        if (type == null) {
            return; // 未 build（onInitEarly 未执行），不属于泄漏
        }
        if (BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.fromNamespaceAndPath("ic2", id))) {
            return; // 已注册
        }
        try {
            net.minecraft.core.Registry.register(BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath("ic2", id), type);
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").warn(
                "ic2 entity '{}' was not registered; fallback-registered", id);
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").error(
                "fallback register '{}' failed: {}", id, t.toString());
        }
    }

    /** 诊断：打印各实体 build/注册状态（供排查 freeze 阶段 intrusive holder 泄漏） */
    public static void dumpBoundState() {
        org.apache.logging.log4j.LogManager.getLogger("ic2-diag").info(
            "Ic2Entities build check: itnt={} nuke={} laser={} plasma={} rubber_boat={} electric_boat={} carbon_boat={}",
            ITNT != null, NUKE != null, LASER_BULLET != null, PLASMA_PARTICLE != null,
            RUBBER_BOAT != null, ELECTRIC_BOAT != null, CARBON_BOAT != null);
        dumpUnregisteredEntityTypes();
    }

    /**
     * 直接 dump ENTITY_TYPE 注册表中"已 build 但未注册"的 intrusive holder（freeze 崩溃元凶）。
     * 反射读取 MappedRegistry.unregisteredIntrusiveHolders，并反射 EntityType.factory 字段
     * 获取构造器来源类名以定位泄漏 mod。必须在 freeze 之前调用。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void dumpUnregisteredEntityTypes() {
        org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger("ic2-diag");
        try {
            java.lang.reflect.Field f = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            f.setAccessible(true);
            java.util.Map<Object, ?> map = (java.util.Map<Object, ?>) f.get(BuiltInRegistries.ENTITY_TYPE);
            log.info("ENTITY_TYPE unregistered intrusive holders count={}", map == null ? -1 : map.size());
            if (map != null) {
                for (Object key : map.keySet()) {
                    String info = String.valueOf(key);
                    try {
                        java.lang.reflect.Field ff = key.getClass().getDeclaredField("factory");
                        ff.setAccessible(true);
                        Object factory = ff.get(key);
                        info = info + " | factory=" + (factory == null ? "null" : factory.getClass().getName());
                    } catch (Throwable ignored) {
                    }
                    log.info("  unregistered EntityType: {}", info);
                }
            }
        } catch (Throwable t) {
            log.warn("dumpUnregisteredEntityTypes failed: {}", t.toString());
        }
    }

    /**
     * 同 {@link #dumpUnregisteredEntityTypes()}，并对每个泄漏 EntityType 暴力注册到
     * `ic2:leaked_fallback_N`（注册表未冻结时可写），避免 freeze 崩溃。
     * 由 MappedRegistryMixin（freeze HEAD）在 ENTITY_TYPE 注册表 freeze 前调用。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void dumpUnregisteredEntityTypesWithFallback() {
        org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger("ic2-diag");
        try {
            java.lang.reflect.Field f = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            f.setAccessible(true);
            java.util.Map<Object, ?> map = (java.util.Map<Object, ?>) f.get(BuiltInRegistries.ENTITY_TYPE);
            int n = map == null ? 0 : map.size();
            log.info("freeze-hook dump -> ENTITY_TYPE unregistered intrusive holders count={}", n);
            if (map != null && n > 0) {
                int i = 0;
                for (Object key : map.keySet()) {
                    String factoryName = "unknown";
                    try {
                        java.lang.reflect.Field ff = key.getClass().getDeclaredField("factory");
                        ff.setAccessible(true);
                        Object factory = ff.get(key);
                        factoryName = factory == null ? "null" : factory.getClass().getName();
                    } catch (Throwable ignored) {
                    }
                    log.info("  unregistered EntityType [{}]: factory={}", i, factoryName);
                    try {
                        net.minecraft.resources.ResourceLocation rl =
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ic2", "leaked_fallback_" + i);
                        net.minecraft.core.Registry.register(BuiltInRegistries.ENTITY_TYPE, rl,
                            (net.minecraft.world.entity.EntityType) key);
                        log.info("    -> fallback-registered to {}", rl);
                    } catch (Throwable t) {
                        log.error("    -> fallback register FAILED: {}", t.toString());
                    }
                    i++;
                }
            }
        } catch (Throwable t) {
            log.warn("dumpUnregisteredEntityTypesWithFallback failed: {}", t.toString());
        }
    }
}
