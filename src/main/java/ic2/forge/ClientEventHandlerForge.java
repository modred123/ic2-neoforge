/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.shaders.FogShape
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.client.event.RenderGuiLayerEvent$Post
 *  net.neoforged.neoforge.client.event.RenderHighlightEvent$Block
 *  net.neoforged.neoforge.client.event.RenderLivingEvent$Post
 *  net.neoforged.neoforge.client.event.RenderLivingEvent$Pre
 *  net.neoforged.neoforge.client.event.ScreenEvent$Init
 *  net.neoforged.neoforge.client.event.ViewportEvent$ComputeFogColor
 *  net.neoforged.neoforge.client.event.ViewportEvent$RenderFog
 *  net.neoforged.neoforge.client.event.sound.PlaySoundEvent
 *  net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent
 *  net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay
 *  net.neoforged.neoforge.event.TickEvent$ClientTickEvent
 *  net.neoforged.neoforge.event.TickEvent$Phase
 *  net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 */
package ic2.forge;

import com.mojang.blaze3d.shaders.FogShape;
import ic2.core.event.EventHandlerClient;
import ic2.core.event.TickHandler;
import ic2.core.proxy.SideProxyClient;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ClientEventHandlerForge {
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre clientTickEvent) {
        if (true) {
            TickHandler.onClientTick();
        }
    }


    @SubscribeEvent
    public void livingEntityPreRender(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> pre) {
        EventHandlerClient.livingEntityPreRender(pre.getEntity(), (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>)pre.getRenderer());
    }

    @SubscribeEvent
    public void livingEntityPostRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> post) {
        EventHandlerClient.livingEntityPostRender(post.getEntity(), (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>)post.getRenderer());
    }

    @SubscribeEvent
    public void onSetupFogDensity(ViewportEvent.RenderFog renderFog) {
        float f = EventHandlerClient.onSetupFogDensity(renderFog.getCamera().getBlockAtCamera());
        if (f >= 0.0f) {
            renderFog.setCanceled(true);
            renderFog.setNearPlaneDistance(-8.0f);
            renderFog.setFarPlaneDistance(f * 0.5f);
            renderFog.setFogShape(FogShape.SPHERE);
        }
    }

    @SubscribeEvent
    public void onRenderFogColor(ViewportEvent.ComputeFogColor computeFogColor) {
        int n = EventHandlerClient.onRenderFogColor(computeFogColor.getCamera().getBlockAtCamera());
        if (n >= 0) {
            computeFogColor.setRed((float)(n >>> 16 & 0xFF) / 255.0f);
            computeFogColor.setGreen((float)(n >>> 8 & 0xFF) / 255.0f);
            computeFogColor.setBlue((float)(n & 0xFF) / 255.0f);
        }
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(RenderHighlightEvent.Block block) {
        EventHandlerClient.onDrawBlockHighlight((Player)SideProxyClient.mc().player, block.getTarget(), block.getDeltaTracker().getGameTimeDeltaPartialTick(false), block.getPoseStack(), block.getMultiBufferSource());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onDrawBlockHighlightLast(RenderHighlightEvent.Block block) {
        if (EventHandlerClient.onDrawBlockHighlightLast((Player)SideProxyClient.mc().player, block.getTarget(), block.getDeltaTracker().getGameTimeDeltaPartialTick(false), block.getPoseStack(), block.getMultiBufferSource())) {
            block.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiCreate(ScreenEvent.Init.Pre init) {
        Screen screen = init.getScreen();
        List list = init.getListenersList();
        EventHandlerClient.onGuiCreate(screen, list, init::addListener);
    }

    @SubscribeEvent
    public void onDrawTooltip(ItemTooltipEvent itemTooltipEvent) {
        EventHandlerClient.onDrawTooltip(itemTooltipEvent.getItemStack(), itemTooltipEvent.getToolTip());
    }

    @SubscribeEvent
    public void onRenderHotBar(RenderGuiLayerEvent.Post post) {
        if (post.getName() == VanillaGuiLayers.HOTBAR) {
            EventHandlerClient.onRenderHotBar();
        }
    }

    @SubscribeEvent
    public void onSoundPlayed(PlaySoundEvent playSoundEvent) {
        SoundInstance soundInstance = playSoundEvent.getSound();
        SoundInstance soundInstance2 = EventHandlerClient.onSoundPlayed(soundInstance);
        if (soundInstance2 != soundInstance) {
            playSoundEvent.setSound(soundInstance2);
        }
    }

    @SubscribeEvent
    public void onDisconnect(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        EventHandlerClient.onDisconnect();
    }
}

