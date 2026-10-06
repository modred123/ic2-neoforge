/*
 * IC2 ex112 -> NeoForge 1.21.1 迁移 — freeze 兜底 Mixin
 *
 * 背景：某些 mod（或 ic2 的类加载副作用）会在 RegisterEvent 全部派发之后、
 * GameData.freezeData 遍历注册表时，才动态构造未注册的 EntityType（intrusive
 * holder 泄漏），导致 MappedRegistry.freeze() 抛
 * "Some intrusive holders were not registered" 崩溃。
 *
 * 本 Mixin 在 MappedRegistry.freeze() 进入时（HEAD，注册表尚未冻结）检查：
 * 若是 ENTITY_TYPE 注册表，则 dump 未注册的 intrusive holder 并对其中的每个
 * EntityType 用"同一实例"暴力注册到 ic2:leaked_fallback_N —— register() 内部
 * 会从 unregisteredIntrusiveHolders 移除该实例的 holder 并绑定，freeze 即可通过。
 */
package ic2.forge.mixin;

import ic2.core.ref.Ic2Entities;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "freeze", at = @At("HEAD"))
    private void ic2$onFreeze(CallbackInfoReturnable<Registry> cir) {
        try {
            MappedRegistry<?> self = (MappedRegistry<?>) (Object) this;
            ResourceKey<? extends Registry<?>> key = self.key();
            if (key != null && key.equals(Registries.ENTITY_TYPE)) {
                // 注册表尚未冻结（freeze 内部先置 frozen=true 前注入点已执行），
                // dump + 暴力兜底注册泄漏的 intrusive holder
                Ic2Entities.dumpUnregisteredEntityTypesWithFallback();
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").error(
                "MappedRegistryMixin freeze hook failed: {}", t.toString());
        }
    }
}
