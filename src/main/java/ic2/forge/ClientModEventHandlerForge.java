/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.renderer.ItemBlockRenderTypes
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.neoforged.neoforge.client.event.ModelEvent$RegisterGeometryLoaders
 *  net.neoforged.neoforge.client.event.RegisterColorHandlersEvent$Block
 *  net.neoforged.neoforge.client.event.RegisterColorHandlersEvent$Item
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 *  net.neoforged.neoforge.client.model.geometry.IGeometryLoader
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
 */
package ic2.forge;

import ic2.forge.ClientEnvProxyForge;
import ic2.forge.model.BeModelLoader;
import ic2.forge.model.CableModelLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import ic2.core.event.EventHandlerClient;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientModEventHandlerForge {
    @SubscribeEvent
    public void onRegisterBlockColorProviders(RegisterColorHandlersEvent.Block block) {
        for (ClientEnvProxyForge.BlockColorProviderRegistration blockColorProviderRegistration : ClientEnvProxyForge.blockColorProviderRegistrations) {
            block.getBlockColors().register(blockColorProviderRegistration.provider(), blockColorProviderRegistration.blocks());
        }
    }

    @SubscribeEvent
    public void onRegisterItemColorProviders(RegisterColorHandlersEvent.Item item) {
        for (ClientEnvProxyForge.ItemColorProviderRegistration itemColorProviderRegistration : ClientEnvProxyForge.itemColorProviderRegistrations) {
            item.getItemColors().register(itemColorProviderRegistration.provider(), itemColorProviderRegistration.items());
        }
    }

    @SubscribeEvent
    public void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers registerRenderers) {
        for (ClientEnvProxyForge.BerRegistration<?> record : ClientEnvProxyForge.berRegistrations) {
            ClientModEventHandlerForge.registerBer(record, registerRenderers);
        }
        for (ClientEnvProxyForge.EntityRendererRegistration entityRendererRegistration : ClientEnvProxyForge.entityRendererRegistrations) {
            ClientModEventHandlerForge.registerEntityRenderer(entityRendererRegistration, registerRenderers);
        }
        for (ClientEnvProxyForge.BlockEntityRendererRegistration blockEntityRendererRegistration : ClientEnvProxyForge.blockEntityRendererRegistrations) {
            ClientModEventHandlerForge.registerBlockEntityRenderer(blockEntityRendererRegistration, registerRenderers);
        }
    }

    private static <T extends BlockEntity> void registerBer(ClientEnvProxyForge.BerRegistration<T> berRegistration, EntityRenderersEvent.RegisterRenderers registerRenderers) {
        registerRenderers.registerBlockEntityRenderer(berRegistration.blockEntityType(), berRegistration.blockEntityRendererProvider());
    }

    private static <T extends Entity> void registerEntityRenderer(ClientEnvProxyForge.EntityRendererRegistration<T> entityRendererRegistration, EntityRenderersEvent.RegisterRenderers registerRenderers) {
        // type 为延迟 Supplier：EntityType 在 ENTITY_TYPE RegisterEvent 之后才绑定，
        // 此处（EntityRenderersEvent.RegisterRenderers）注册表已冻结，.get() 必然成功
        registerRenderers.registerEntityRenderer(entityRendererRegistration.type().get(), entityRendererRegistration.factory());
    }

    private static <T extends BlockEntity> void registerBlockEntityRenderer(ClientEnvProxyForge.BlockEntityRendererRegistration<T> blockEntityRendererRegistration, EntityRenderersEvent.RegisterRenderers registerRenderers) {
        registerRenderers.registerBlockEntityRenderer(blockEntityRendererRegistration.type(), blockEntityRendererRegistration.factory());
    }

    @SubscribeEvent
    public void onModelRegistry(ModelEvent.RegisterGeometryLoaders registerGeometryLoaders) {
        registerGeometryLoaders.register(ResourceLocation.fromNamespaceAndPath("ic2", "be"), new BeModelLoader());
        registerGeometryLoaders.register(ResourceLocation.fromNamespaceAndPath("ic2", "cable"), new CableModelLoader());
    }

    @SubscribeEvent
    public void onRegisterKeybindings(RegisterKeyMappingsEvent registerKeyMappingsEvent) {
        for (KeyMapping keyMapping : ClientEnvProxyForge.keyBindingRegistrations) {
            registerKeyMappingsEvent.register(keyMapping);
        }
    }


    @SubscribeEvent
    public void onSoundSetup(SoundEngineLoadEvent soundEngineLoadEvent) {
        EventHandlerClient.onSoundSetup(soundEngineLoadEvent.getEngine());
    }

    @SubscribeEvent
    public void onRegisterScreens(RegisterMenuScreensEvent registerMenuScreensEvent) {
        for (ClientEnvProxyForge.ScreenRegistration<?> screenRegistration : ClientEnvProxyForge.screenRegistrations) {
            ClientModEventHandlerForge.registerScreen(screenRegistration, registerMenuScreensEvent);
        }
    }

    private static <H extends net.minecraft.world.inventory.AbstractContainerMenu> void registerScreen(ClientEnvProxyForge.ScreenRegistration<H> screenRegistration, RegisterMenuScreensEvent registerMenuScreensEvent) {
        registerMenuScreensEvent.register(screenRegistration.menuType(), screenRegistration.screenFactory()::create);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent fMLClientSetupEvent) {
        // 第二十八轮修复（待办 B 根因）：补调客户端环境初始化。
        // 1.19.2 起本方法只保留了 setRenderLayer，漏掉了 EventHandlerClient.onClientSetup()，
        // 于是 ic2:charge（电池电量贴图）、六向（高级升级模块朝向）、nano_saber_active（纳米剑）
        // 三个模型 predicate 从未注册 —— 电池贴图因此永远停在 _0（空电）那一档。
        EventHandlerClient.onClientSetup();
        for (ClientEnvProxyForge.BlockLayerRegistration blockLayerRegistration : ClientEnvProxyForge.blockLayerRegistrations) {
            for (Block block : blockLayerRegistration.blocks()) {
                ItemBlockRenderTypes.setRenderLayer((Block)block, (RenderType)blockLayerRegistration.layer());
            }
        }
    }
}

