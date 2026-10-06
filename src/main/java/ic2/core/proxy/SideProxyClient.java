/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.color.block.BlockColor
 *  net.minecraft.client.color.item.ItemColor
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.proxy;

import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.AudioManagerClient;
import ic2.core.audio.PositionSpec;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerWaterKineticGenerator;
import ic2.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.block.machine.container.ContainerCondenser;
import ic2.core.block.machine.container.ContainerCropHarvester;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.block.machine.container.ContainerFluidRegulator;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.machine.container.ContainerItemBuffer;
import ic2.core.block.machine.container.ContainerLiquidHeatExchanger;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.block.machine.container.ContainerPatternStorage;
import ic2.core.block.machine.container.ContainerReplicator;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.block.machine.container.ContainerWeightedItemDistributor;
import ic2.core.block.personal.ContainerEnergyOMatClosed;
import ic2.core.block.personal.ContainerEnergyOMatOpen;
import ic2.core.block.personal.ContainerTradeOMatClosed;
import ic2.core.block.personal.ContainerTradeOMatOpen;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.block.wiring.ContainerChargepadBlock;
import ic2.core.block.wiring.ContainerElectricBlock;
import ic2.core.block.wiring.ContainerTransformer;
import ic2.core.entity.render.BoatEntityRenderer;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.item.tool.ContainerContainmentbox;
import ic2.core.item.tool.ContainerCropnalyzer;
import ic2.core.item.tool.ContainerMeter;
import ic2.core.item.tool.ContainerToolScanner;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.upgrade.AdvancedUpgradeScreenFactory;
import ic2.core.item.upgrade.HandHeldOre;
import ic2.core.item.upgrade.HandHeldValueConfig;
import ic2.core.block.renderer.KineticGeneratorRenderer;
import ic2.core.entity.render.ExplosiveBlockRenderer;
import ic2.core.entity.render.LaserBulletEntityRenderer;
import ic2.core.proxy.ClientEnvProxy;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import ic2.core.block.heatgenerator.gui.GuiElectricHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiFluidHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiRTHeatGenerator;
import ic2.core.block.kineticgenerator.gui.GuiElectricKineticGenertor;
import ic2.core.block.kineticgenerator.gui.GuiSteamKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import ic2.core.block.machine.gui.GuiAdvMiner;
import ic2.core.block.machine.gui.GuiBatchCrafter;
import ic2.core.block.machine.gui.GuiCanner;
import ic2.core.block.machine.gui.GuiChunkLoader;
import ic2.core.block.machine.gui.GuiClassicCanner;
import ic2.core.block.machine.gui.GuiClassicCropmatron;
import ic2.core.block.machine.gui.GuiCondenser;
import ic2.core.block.machine.gui.GuiCropHarvester;
import ic2.core.block.machine.gui.GuiCropmatron;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.block.machine.gui.GuiFermenter;
import ic2.core.block.machine.gui.GuiFluidBottler;
import ic2.core.block.machine.gui.GuiFluidDistributor;
import ic2.core.block.machine.gui.GuiFluidRegulator;
import ic2.core.block.machine.gui.GuiIndustrialWorkbench;
import ic2.core.block.machine.gui.GuiItemBuffer;
import ic2.core.block.machine.gui.GuiLiquidHeatExchanger;
import ic2.core.block.machine.gui.GuiMagnetizer;
import ic2.core.block.machine.gui.GuiMatter;
import ic2.core.block.machine.gui.GuiMetalFormer;
import ic2.core.block.machine.gui.GuiMiner;
import ic2.core.block.machine.gui.GuiPatternStorage;
import ic2.core.block.machine.gui.GuiReplicator;
import ic2.core.block.machine.gui.GuiScanner;
import ic2.core.block.machine.gui.GuiSolarDestiller;
import ic2.core.block.machine.gui.GuiSortingMachine;
import ic2.core.block.machine.gui.GuiSteamGenerator;
import ic2.core.block.machine.gui.GuiWeightedFluidDistributor;
import ic2.core.block.machine.gui.GuiWeightedItemDistributor;
import ic2.core.block.personal.GuiEnergyOMatClosed;
import ic2.core.block.personal.GuiEnergyOMatOpen;
import ic2.core.block.personal.GuiTradeOMatClosed;
import ic2.core.block.personal.GuiTradeOMatOpen;
import ic2.core.block.reactor.gui.GuiNuclearReactor;
import ic2.core.block.wiring.GuiChargepadBlock;
import ic2.core.block.wiring.GuiElectricBlock;
import ic2.core.block.wiring.GuiTransformer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.item.tool.GuiContainmentbox;
import ic2.core.item.tool.GuiCropnalyzer;
import ic2.core.item.tool.GuiToolMeter;
import ic2.core.item.tool.GuiToolScanner;
import ic2.core.item.tool.GuiToolbox;
import ic2.core.proxy.SideProxy;
import ic2.core.proxy.SideProxyServer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.sound.SoundManager;
import ic2.core.sound.SoundManagerClient;
import ic2.core.util.Keyboard;
import ic2.core.util.KeyboardClient;
import ic2.core.util.Util;
import java.io.File;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class SideProxyClient
implements SideProxy {
    public static final ClientEnvProxy envProxy = SideProxyClient.getEnvProxy();
    /**
     * 1.21 迁移修复：原为 static final `mc = Minecraft.getInstance()`，类加载时机（IC2 静态块，
     * mod 构造期）贴近/早于 Minecraft 实例创建，可能永久捕获 null（Round 13 实测 sendC2SPacket
     * 因此静默丢包）。改为懒获取 mc()：每次访问实时取实例，取到后缓存；
     * 所有调用点已同步改为 SideProxyClient.mc()。
     */
    private static Minecraft mcCached;
    public static Minecraft mc() {
        Minecraft minecraft = mcCached;
        if (minecraft == null) {
            minecraft = Minecraft.getInstance();
            mcCached = minecraft;
        }
        return minecraft;
    }
    private static final AudioManager audioManager = new AudioManagerClient();
    private static final SoundManager soundManager = new SoundManagerClient();
    private static final Keyboard keyboard = new KeyboardClient();

    @Override
    public void preInit() {
        envProxy.registerScreen(Ic2ScreenHandlers.DYNAMIC_BE, (menu, inventory, component) -> DynamicGui.create(menu, inventory, component));
        envProxy.registerScreen(Ic2ScreenHandlers.DYNAMIC_ITEM, (menu, inventory, component) -> DynamicGui.create(menu, inventory, component));
        envProxy.registerScreen(Ic2ScreenHandlers.ELECTRIC_HEAT_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerElectricHeatGenerator>)GuiElectricHeatGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.FLUID_HEAT_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerFluidHeatGenerator>)GuiFluidHeatGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.RT_HEAT_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerRTHeatGenerator>)GuiRTHeatGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ELECTRIC_KINETIC_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerElectricKineticGenerator>)GuiElectricKineticGenertor::new);
        envProxy.registerScreen(Ic2ScreenHandlers.STEAM_KINETIC_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerSteamKineticGenerator>)GuiSteamKineticGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.STIRLING_KINETIC_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerStirlingKineticGenerator>)GuiStirlingKineticGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.WATER_KINETIC_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerWaterKineticGenerator>)GuiWaterKineticGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.WIND_KINETIC_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerWindKineticGenerator>)GuiWindKineticGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.NUCLEAR_REACTOR, (ClientEnvProxy.ScreenFactory<ContainerNuclearReactor>)GuiNuclearReactor::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CONDENSER, (ClientEnvProxy.ScreenFactory<ContainerCondenser>)GuiCondenser::new);
        envProxy.registerScreen(Ic2ScreenHandlers.FLUID_BOTTLER, (ClientEnvProxy.ScreenFactory<ContainerFluidBottler>)GuiFluidBottler::new);
        envProxy.registerScreen(Ic2ScreenHandlers.FLUID_DISTRIBUTOR, (ClientEnvProxy.ScreenFactory<ContainerFluidDistributor>)GuiFluidDistributor::new);
        envProxy.registerScreen(Ic2ScreenHandlers.FLUID_REGULATOR, (ClientEnvProxy.ScreenFactory<ContainerFluidRegulator>)GuiFluidRegulator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.LIQUID_HEAT_EXCHANGER, (ClientEnvProxy.ScreenFactory<ContainerLiquidHeatExchanger>)GuiLiquidHeatExchanger::new);
        envProxy.registerScreen(Ic2ScreenHandlers.SOLAR_DISTILLER, (ClientEnvProxy.ScreenFactory<ContainerSolarDestiller>)GuiSolarDestiller::new);
        envProxy.registerScreen(Ic2ScreenHandlers.STEAM_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerSteamGenerator>)GuiSteamGenerator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ITEM_BUFFER, (ClientEnvProxy.ScreenFactory<ContainerItemBuffer>)GuiItemBuffer::new);
        envProxy.registerScreen(Ic2ScreenHandlers.MAGNETIZER, (ClientEnvProxy.ScreenFactory<ContainerMagnetizer>)GuiMagnetizer::new);
        envProxy.registerScreen(Ic2ScreenHandlers.SORTING_MACHINE, (ClientEnvProxy.ScreenFactory<ContainerSortingMachine>)GuiSortingMachine::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CANNER, (ClientEnvProxy.ScreenFactory<ContainerCanner>)GuiCanner::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CLASSIC_CANNER, (ClientEnvProxy.ScreenFactory<ContainerClassicCanner>)GuiClassicCanner::new);
        envProxy.registerScreen(Ic2ScreenHandlers.FERMENTER, (ClientEnvProxy.ScreenFactory<ContainerFermenter>)GuiFermenter::new);
        envProxy.registerScreen(Ic2ScreenHandlers.METAL_FORMER, (ClientEnvProxy.ScreenFactory<ContainerMetalFormer>)GuiMetalFormer::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_MINER, (ClientEnvProxy.ScreenFactory<ContainerAdvMiner>)GuiAdvMiner::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CROP_HARVESTER, (ClientEnvProxy.ScreenFactory<ContainerCropHarvester>)GuiCropHarvester::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CROPMATRON, (ClientEnvProxy.ScreenFactory<ContainerCropmatron>)GuiCropmatron::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CLASSIC_CROPMATRON, (ClientEnvProxy.ScreenFactory<ContainerClassicCropmatron>)GuiClassicCropmatron::new);
        envProxy.registerScreen(Ic2ScreenHandlers.MINER, (ClientEnvProxy.ScreenFactory<ContainerMiner>)GuiMiner::new);
        envProxy.registerScreen(Ic2ScreenHandlers.MATTER_GENERATOR, (ClientEnvProxy.ScreenFactory<ContainerMatter>)GuiMatter::new);
        envProxy.registerScreen(Ic2ScreenHandlers.PATTERN_STORAGE, (ClientEnvProxy.ScreenFactory<ContainerPatternStorage>)GuiPatternStorage::new);
        envProxy.registerScreen(Ic2ScreenHandlers.REPLICATOR, (ClientEnvProxy.ScreenFactory<ContainerReplicator>)GuiReplicator::new);
        envProxy.registerScreen(Ic2ScreenHandlers.UU_SCANNER, (ClientEnvProxy.ScreenFactory<ContainerScanner>)GuiScanner::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ENERGY_O_MAT_CLOSED, (ClientEnvProxy.ScreenFactory<ContainerEnergyOMatClosed>)GuiEnergyOMatClosed::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ENERGY_O_MAT_OPEN, (ClientEnvProxy.ScreenFactory<ContainerEnergyOMatOpen>)GuiEnergyOMatOpen::new);
        envProxy.registerScreen(Ic2ScreenHandlers.TRADE_O_MAT_CLOSED, (ClientEnvProxy.ScreenFactory<ContainerTradeOMatClosed>)GuiTradeOMatClosed::new);
        envProxy.registerScreen(Ic2ScreenHandlers.TRADE_O_MAT_OPEN, (ClientEnvProxy.ScreenFactory<ContainerTradeOMatOpen>)GuiTradeOMatOpen::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CHARGEPAD, (ClientEnvProxy.ScreenFactory<ContainerChargepadBlock>)GuiChargepadBlock::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ENERGY_STORAGE, (ClientEnvProxy.ScreenFactory<ContainerElectricBlock>)GuiElectricBlock::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ELECTROLYZER, (ClientEnvProxy.ScreenFactory<ContainerElectrolyzer>)GuiElectrolyzer::new);
        envProxy.registerScreen(Ic2ScreenHandlers.TRANSFORMER, (ClientEnvProxy.ScreenFactory<ContainerTransformer>)GuiTransformer::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CHUNK_LOADER, (ClientEnvProxy.ScreenFactory<ContainerChunkLoader>)GuiChunkLoader::new);
        envProxy.registerScreen(Ic2ScreenHandlers.WEIGHTED_FLUID_DISTRIBUTOR, (ClientEnvProxy.ScreenFactory<ContainerWeightedFluidDistributor>)GuiWeightedFluidDistributor::new);
        envProxy.registerScreen(Ic2ScreenHandlers.WEIGHTED_ITEM_DISTRIBUTOR, (ClientEnvProxy.ScreenFactory<ContainerWeightedItemDistributor>)GuiWeightedItemDistributor::new);
        envProxy.registerScreen(Ic2ScreenHandlers.INDUSTRIAL_WORKBENCH, (ClientEnvProxy.ScreenFactory<ContainerIndustrialWorkbench>)GuiIndustrialWorkbench::new);
        envProxy.registerScreen(Ic2ScreenHandlers.BATCH_CRAFTER, (ClientEnvProxy.ScreenFactory<ContainerBatchCrafter>)GuiBatchCrafter::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_UPGRADE, new AdvancedUpgradeScreenFactory());
        envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_UPGRADE_EDIT_ORE, (ClientEnvProxy.ScreenFactory<HandHeldOre.ContainerEditOre>)HandHeldOre.GuiEditOre::new);
        envProxy.registerScreen(Ic2ScreenHandlers.ADVANCED_UPGRADE_VALUE_CONFIG, (ClientEnvProxy.ScreenFactory<HandHeldValueConfig.ContainerValueConfig>)HandHeldValueConfig.GuiValueConfig::new);
        envProxy.registerScreen(Ic2ScreenHandlers.SCANNER, (ClientEnvProxy.ScreenFactory<ContainerToolScanner>)GuiToolScanner::new);
        /*
         * 第三十三轮修复（右键闪退根因）：这四个手持 GUI 原先都没有自己的界面工厂 ——
         * 电表/工具箱/防辐射箱共用 DYNAMIC_ITEM（工厂 = DynamicGui.create，第一步就把 menu 强转
         * DynamicContainer → ClassCastException → 玩家被踢出）；作物分析仪的 CROP_ANALYZER 则
         * 完全没有注册界面工厂（点了没反应）。各自注册后与 1.12.2 的 GuiHandler 一一对应。
         */
        envProxy.registerScreen(Ic2ScreenHandlers.METER, (ClientEnvProxy.ScreenFactory<ContainerMeter>)GuiToolMeter::new);
        envProxy.registerScreen(Ic2ScreenHandlers.TOOLBOX, (ClientEnvProxy.ScreenFactory<ContainerToolbox>)GuiToolbox::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CONTAINMENT_BOX, (ClientEnvProxy.ScreenFactory<ContainerContainmentbox>)GuiContainmentbox::new);
        envProxy.registerScreen(Ic2ScreenHandlers.CROP_ANALYZER, (ClientEnvProxy.ScreenFactory<ContainerCropnalyzer>)GuiCropnalyzer::new);
        envProxy.registerColorProvider(new BlockColor(){

            public int getColor(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, int n) {
                return 0x669944;
            }
        }, new Block[]{Ic2Blocks.RUBBER_LEAVES});
        envProxy.registerColorProvider(new ItemColor(){

            public int getColor(ItemStack itemStack, int n) {
                return 0x669944;
            }
        }, new ItemLike[]{Ic2Items.RUBBER_LEAVES});
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.RUBBER_SAPLING);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.WOODEN_SCAFFOLD, Ic2Blocks.IRON_SCAFFOLD);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.FOAM);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.REINFORCED_GLASS);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.REINFORCED_DOOR);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.COPPER_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_COPPER_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TIN_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_TIN_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.IRON_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_IRON_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DOUBLE_INSULATED_IRON_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TRIPLE_INSULATED_IRON_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.GOLD_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.INSULATED_GOLD_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DOUBLE_INSULATED_GOLD_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.GLASS_FIBRE_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DETECTOR_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.SPLITTER_CABLE);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CROP_STICK);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.WEED_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.WHEAT_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CARROTS_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.POTATO_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BEETROOTS_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.PUMPKIN_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.MELON_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DANDELION_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.POPPY_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BLACKTHORN_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TULIP_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CYAZINT_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.VENOMILIA_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.REED_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.STICKY_REED_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.COCOA_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.FLAX_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.RED_MUSHROOM_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BROWN_MUSHROOM_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.NETHER_WART_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.TERRA_WART_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.OAK_SAPLING_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.SPRUCE_SAPLING_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.BIRCH_SAPLING_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.JUNGLE_SAPLING_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.ACACIA_SAPLING_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.DARK_OAK_SAPLING_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.FERRU_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.CYPRIUM_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.STAGNIUM_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.PLUMBISCUS_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.AURELIA_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.SHINING_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.RED_WHEAT_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.COFFEE_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.HOPS_CROP);
        envProxy.registerBlockLayer(RenderType.cutoutMipped(), Ic2Blocks.EATING_PLANT_CROP);
        this.registerRotorProvider(Ic2BlockEntities.WIND_KINETIC_GENERATOR);
        this.registerRotorProvider(Ic2BlockEntities.WATER_KINETIC_GENERATOR);
        this.registerRotorProvider(Ic2BlockEntities.WIND_GENERATOR);
        this.registerRotorProvider(Ic2BlockEntities.WATER_GENERATOR);
        // EntityType 由 Ic2Entities 统一缓冲模式在 ENTITY_TYPE RegisterEvent 时才绑定，
        // 此处（onInitEarly，RegisterEvent 阶段）不能直接求值，改传延迟 Supplier，
        // 真正取实体类型推迟到 EntityRenderersEvent.RegisterRenderers（注册表已冻结）。
        envProxy.registerEntityRenderer(() -> Ic2Entities.ITNT, ExplosiveBlockRenderer::new);
        envProxy.registerEntityRenderer(() -> Ic2Entities.NUKE, ExplosiveBlockRenderer::new);
        envProxy.registerEntityRenderer(() -> Ic2Entities.LASER_BULLET, LaserBulletEntityRenderer::new);
        envProxy.registerEntityRenderer(() -> Ic2Entities.RUBBER_BOAT, context -> new BoatEntityRenderer(context, false, "ic2"));
        envProxy.registerEntityRenderer(() -> Ic2Entities.ELECTRIC_BOAT, context -> new BoatEntityRenderer(context, false, "ic2"));
        envProxy.registerEntityRenderer(() -> Ic2Entities.CARBON_BOAT, context -> new BoatEntityRenderer(context, false, "ic2"));
        envProxy.registerBlockEntityRenderer(Ic2BlockEntities.SIGN, net.minecraft.client.renderer.blockentity.SignRenderer::new);
    }

    @Override
    public void onPostInit() {
    }

    @Override
    public boolean isSimulating() {
        return !this.isRendering();
    }

    @Override
    @Deprecated
    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    public SoundManager getSoundManager() {
        return soundManager;
    }

    @Override
    public Keyboard getKeyboard() {
        return keyboard;
    }

    @Override
    public boolean isRendering() {
        return Minecraft.getInstance().isSameThread();
    }

    @Override
    public void requestTick(boolean bl, Runnable runnable) {
        if (bl) {
            IntegratedServer integratedServer = mc().getSingleplayerServer();
            if (integratedServer != null) {
                integratedServer.execute(runnable);
            } else {
                // 1.21 迁移修复：onInitLate 于加载完成时调度，此时玩家可能仍在标题界面
                // （单机尚未开服）。原实现直接 throw IllegalStateException("server unavailable")
                // 导致 ChunkLoadAwareBlockHandler::init 丢失调度、EnergyNet 初始化链断掉。
                // 改为先挂到 client 线程重试：进入存档后 integratedServer 必然已存在。
                mc().execute(() -> {
                    IntegratedServer retry = mc().getSingleplayerServer();
                    if (retry != null) {
                        retry.execute(runnable);
                    } else {
                        // 纯 client（连接远端服务器）不会真正需要 server tick；在客户端线程直接执行。
                        // 该分支唯一任务（ChunkLoadAwareBlockHandler::init）仅建状态映射表，两侧均可安全执行。
                        runnable.run();
                    }
                });
            }
        } else {
            mc().execute(runnable);
        }
    }

    @Override
    public void onServerAvailable(MinecraftServer minecraftServer) {
    }

    @Override
    public void displayError(String string, Object ... objectArray) {
        SideProxyServer.displayError0(string, objectArray);
    }

    @Override
    public void displayError(Exception exception, String string, Object ... objectArray) {
        SideProxyServer.displayError(this, exception, string, objectArray);
    }

    @Override
    public Player getPlayerInstance() {
        return SideProxyClient.mc().player;
    }

    @Override
    public Level getWorld(MinecraftServer minecraftServer, ResourceLocation resourceLocation) {
        if (minecraftServer == null) {
            ClientLevel clientLevel = SideProxyClient.mc().level;
            if (clientLevel != null && resourceLocation.equals((Object)Util.getDimId((Level)clientLevel))) {
                return clientLevel;
            }
        } else {
            for (Level level : minecraftServer.getAllLevels()) {
                if (!resourceLocation.equals((Object)Util.getDimId(level))) continue;
                return level;
            }
        }
        return null;
    }

    @Override
    public Level getPlayerWorld() {
        return SideProxyClient.mc().level;
    }

    @Override
    public RecipeManager getRecipeManager() {
        IntegratedServer integratedServer = mc().getSingleplayerServer();
        if (integratedServer != null) {
            return integratedServer.getRecipeManager();
        }
        return Objects.requireNonNull(mc().getConnection()).getRecipeManager();
    }

    @Override
    public File getMinecraftDir() {
        return envProxy.getMinecraftDir();
    }

    @Override
    @Deprecated
    public void playSoundSp(String string, float f, float f2) {
        IC2.audioManager.playOnce(this.getPlayerInstance(), PositionSpec.Hand, string, true, IC2.audioManager.getDefaultVolume());
    }

    @Override
    public void playSoundOnce(Entity entity, SoundEvent soundEvent, float f, float f2) {
        entity.playSound(soundEvent, f, f2);
    }

    @Override
    public void messagePlayer(Player player, String string, Object ... objectArray) {
        if (player == null) {
            player = SideProxyClient.mc().player;
        }
        if (objectArray.length > 0) {
            player.displayClientMessage((Component)Component.translatable((String)string, (Object[])SideProxyServer.getMessageComponents(objectArray)), false);
        } else {
            player.displayClientMessage((Component)Component.literal((String)string), false);
        }
    }

    @Override
    public <T extends BlockEntity> void registerRotorProvider(BlockEntityType<T> blockEntityType) {
        envProxy.registerBer(blockEntityType, KineticGeneratorRenderer::new);
    }

    private static ClientEnvProxy getEnvProxy() {
        try {
            if (IC2.envProxy.isFabricEnv()) {
                return (ClientEnvProxy)Class.forName("ic2.fabric.ClientEnvProxyFabric").getConstructors()[0].newInstance(new Object[0]);
            }
            if (IC2.envProxy.isForgeEnv()) {
                return (ClientEnvProxy)Class.forName("ic2.forge.ClientEnvProxyForge").getConstructors()[0].newInstance(new Object[0]);
            }
            throw new IllegalStateException("unknown env");
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

}

