/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.item.ClampedItemPropertyFunction
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Registry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 */
package ic2.core.event;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.item.ElectricItem;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.audio.AudioManagerClient;
import ic2.core.fluid.FluidHandler;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.network.RpcHandler;
import ic2.core.proxy.SideProxyClient;
import ic2.core.sound.SoundManagerClient;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EventHandlerClient {
    public static void onClientSetup() {
        SideProxyClient.envProxy.registerModelPredicateProvider(IC2.getIdentifier("charge"), (itemStack, clientLevel, livingEntity, n) -> (float)ElectricItem.manager.getChargeLevel(itemStack));
        for (Direction direction : Util.ALL_DIRS) {
            SideProxyClient.envProxy.registerModelPredicateProvider(IC2.getIdentifier(direction.getName()), (itemStack, clientLevel, livingEntity, n) -> itemStack.getItem() instanceof ItemUpgradeModule && ItemUpgradeModule.getDirection(itemStack) == direction ? 1.0f : 0.0f);
        }
        SideProxyClient.envProxy.registerModelPredicateProvider(IC2.getIdentifier("nano_saber_active"), (itemStack, clientLevel, livingEntity, n) -> { Item item = itemStack.getItem(); if (item instanceof AbstractItemNanoSaber) { return ((AbstractItemNanoSaber)item).getActiveData(); } return 0.0f; });
    }

    public static void onSoundSetup(net.minecraft.client.sounds.SoundEngine engine) {
        ((AudioManagerClient)IC2.sideProxy.getAudioManager()).onSoundSetup(engine);
    }

    public static void livingEntityPreRender(LivingEntity livingEntity, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingEntityRenderer) {
    }

    public static void livingEntityPostRender(LivingEntity livingEntity, LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> livingEntityRenderer) {
    }

    public static float onSetupFogDensity(BlockState blockState) {
        Fluid fluid = FluidHandler.getWorldFluid(blockState);
        if (fluid != null && "ic2".equals(BuiltInRegistries.FLUID.getKey(fluid).getNamespace())) {
            int n = FluidHandler.getDensity(fluid);
            return (float)Util.map(Math.abs(n), 20000.0, 2.0);
        }
        return -1.0f;
    }

    public static int onRenderFogColor(BlockState blockState) {
        Fluid fluid = FluidHandler.getWorldFluid(blockState);
        if (fluid != null && "ic2".equals(BuiltInRegistries.FLUID.getKey(fluid).getNamespace())) {
            int n = FluidHandler.getColor(fluid);
            return n;
        }
        return -1;
    }

    public static void onDrawBlockHighlight(Player player, BlockHitResult blockHitResult, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        assert (blockHitResult.getType() == HitResult.Type.BLOCK);
        ItemStack itemStack = StackUtil.get(player, InteractionHand.MAIN_HAND);
        if (itemStack.getItem() instanceof IEnhancedOverlayProvider) {
            // empty if block
        }
    }

    public static boolean onDrawBlockHighlightLast(Player player, BlockHitResult blockHitResult, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        assert (blockHitResult.getType() == HitResult.Type.BLOCK);
        Level level = player.getCommandSenderWorld();
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return false;
    }

    public static void onGuiCreate(Screen screen, List<GuiEventListener> list, Consumer<GuiEventListener> consumer) {
    }

    public static void onDrawTooltip(ItemStack itemStack, List<Component> list) {
        // 1.21.1 修复（第二十四轮）：补齐电量 tooltip 的**全局**入口。
        //
        // 1.12.2（权威实现）中 ElectricItemTooltipHandler 是事件处理器：构造器内
        // MinecraftForge.EVENT_BUS.register(this)，监听 ItemTooltipEvent，对所有带电物品
        // 自动追加 "已充/总量 EU" 行 —— 电池、电芯、扫描仪、喷气背包一视同仁。
        //
        // 1.19.2 起该类被改成静态工具方法，只能由物品各自手动调用；而实际上只有
        // ItemArmorElectric 与 ItemElectricTool 两处调用了它，导致**电池类物品
        // （ItemBattery / ItemBatteryChargeHotbar / ItemClassicCell 等）完全没有电量提示**，
        // 而本方法自 1.19.2 起一直是空实现。此处恢复 1.12.2 的全局行为。
        ic2.core.item.ElectricItemTooltipHandler.addTooltip(itemStack, list);
        // 第四十轮：方块物品的「能量等级 / 输出 / 最大电量」——1.12.2 由 ItemBlockTe → dummy TE 转发，
        // 1.21 的普通 BlockItem 没有任何人调用 TE.addInformation，这里补桥（详见 EnergyTierTooltipHandler）。
        ic2.core.item.EnergyTierTooltipHandler.addTooltip(itemStack, list);
    }

    public static void onRenderHotBar() {
    }

    public static SoundInstance onSoundPlayed(SoundInstance soundInstance) {
        return SoundManagerClient.onSoundPlayed(soundInstance);
    }

    public static void onDisconnect() {
        RpcHandler.onDisconnect();
    }

}

