package ic2.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Ic2DamageSource {
    public static final ResourceKey<DamageType> ELECTRICITY = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.getIdentifier("electricity"));
    public static final ResourceKey<DamageType> NUKE = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.getIdentifier("nuke"));
    public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.getIdentifier("radiation"));
    public static final ResourceKey<DamageType> CROP_EATING = ResourceKey.create(Registries.DAMAGE_TYPE, IC2.getIdentifier("crop_eating"));

    public static DamageSource electricity(Level level) {
        return level.damageSources().source(ELECTRICITY);
    }

    public static DamageSource nuke(Level level) {
        return level.damageSources().source(NUKE);
    }

    public static DamageSource radiation(Level level) {
        return level.damageSources().source(RADIATION);
    }

    public static DamageSource cropEating(Level level) {
        return level.damageSources().source(CROP_EATING);
    }

    /**
     * 1.12.2 权威（`IC2DamageSource.getNukeSource(Explosion)`）：igniter 非空 → 归属到 igniter 的
     * "nuke.player"；igniter 为空 → 通用 "nuke"。**两条路径都必须返回非 null**。
     *
     * 第三十七轮修复：迁移版写成 `livingEntity != null ? livingEntity.damageSources()… : null`，两个问题：
     *  ① igniter 为 null 时返回 null → `Ic2Explosion.damageSource` 为 null → 后面 `entity.hurt(damageSource,…)` NPE；
     *  ② 取 DamageSources 用的是可能为 null 的 `livingEntity`（应改用 level）。
     */
    public static DamageSource getNukeSource(Level level, LivingEntity livingEntity) {
        return livingEntity != null
                ? level.damageSources().source(NUKE, livingEntity)
                : level.damageSources().source(NUKE);
    }
}
