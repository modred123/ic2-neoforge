package ic2.core;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Ic2Potion extends MobEffect {
    public static Ic2Potion radiation;

    public Ic2Potion(MobEffectCategory category, int liquidColor) {
        super(category, liquidColor);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (this == radiation) {
            entity.hurt(Ic2DamageSource.radiation(entity.level()), (amplifier / 100) + 0.5f);
        }
        return false;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        if (this == radiation) {
            int rate = 25 >> amplifier;
            return rate > 0 ? duration % rate == 0 : true;
        }
        return false;
    }

    /**
     * 第三十二轮修复：原来用 Holder.direct(this) 构造 MobEffectInstance。
     * Holder.direct 与注册表 Holder 是两个不同的实现，hashCode/equals 都不相等，
     * 因此玩家身上（由网络包按注册表 id 还原）的效果永远查不到 ——
     * LivingEntity.getEffect(Holder.direct(x)) 恒返回 null，碘片/地球疣因此失效。
     * 统一改用注册表 Holder（未注册时 wrapAsHolder 会退化为 Direct，行为与旧版一致）。
     */
    public net.minecraft.core.Holder<MobEffect> getHolder() {
        return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
    }

    public void applyTo(LivingEntity entity, int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(this.getHolder(), duration, amplifier));
    }
}
