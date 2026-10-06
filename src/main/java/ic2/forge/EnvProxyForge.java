/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.data.worldgen.features.FeatureUtils
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.MenuType$MenuSupplier
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.WoodType
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.levelgen.GenerationStep$Decoration
 *  net.minecraft.world.level.levelgen.feature.ConfiguredFeature
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
 *  net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
 *  net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
 *  net.minecraft.world.level.levelgen.placement.PlacedFeature
 *  net.minecraft.world.level.levelgen.placement.PlacementModifier
 *  net.minecraft.world.level.levelgen.placement.PlacementModifierType
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.ForgeHooks
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.common.extensions.IMenuTypeExtension
 *  net.neoforged.neoforge.common.util.FakePlayer
 *  net.neoforged.neoforge.common.util.FakePlayerFactory
 *  net.neoforged.bus.api.Event
 *  net.neoforged.fml.loading.FMLEnvironment
 *  net.neoforged.neoforge.network.IContainerFactory
 *  net.neoforged.neoforge.network.NetworkHooks
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.ForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.server.ServerLifecycleHooks
 */
package ic2.forge;

import net.minecraft.core.registries.Registries;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import ic2.api.energy.ProfileEvent;
import ic2.api.event.ExplosionEvent;
import ic2.api.event.RetextureEvent;
import ic2.core.IC2;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.item.EnvItemHandler;
import ic2.core.network.GrowingBuffer;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2CreativeVariants;
import ic2.forge.EnvFluidHandlerForge;
import ic2.forge.EnvItemHandlerForge;
import ic2.forge.FmlMod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class EnvProxyForge
implements EnvProxy {
    // 统一延迟注册缓冲：每个条目 [ResourceKey, ResourceLocation, Supplier]
    static final List<Object[]> pendingRegistrations = new ArrayList<Object[]>();
    static HashMap<Item, Integer> burnTimeRecord = new HashMap();
    private static final boolean isClient = FMLEnvironment.dist.isClient();

    @Override
    public boolean isClientEnv() {
        return isClient;
    }

    @Override
    public boolean isFabricEnv() {
        return false;
    }

    @Override
    public boolean isForgeEnv() {
        return true;
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public void registerBlock(ResourceLocation resourceLocation, Block block) {
        pendingRegistrations.add(new Object[]{Registries.BLOCK, resourceLocation, (java.util.function.Supplier<Block>)() -> block});
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(ResourceLocation resourceLocation, BiFunction<BlockPos, BlockState, T> biFunction, Block ... blockArray) {
        BiFunction<BlockPos, BlockState, T> biFunction2 = biFunction;
        Objects.requireNonNull(biFunction2);
        BlockEntityType blockEntityType = BlockEntityType.Builder.of((BlockEntityType.BlockEntitySupplier)(BlockEntityType.BlockEntitySupplier)biFunction2::apply, (Block[])blockArray).build(null);
        pendingRegistrations.add(new Object[]{Registries.BLOCK_ENTITY_TYPE, resourceLocation, (java.util.function.Supplier<BlockEntityType>)() -> blockEntityType});
        return blockEntityType;
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> registerScreenHandler(ResourceLocation resourceLocation, BiFunction<Integer, Inventory, T> biFunction) {
        BiFunction<Integer, Inventory, T> biFunction2 = biFunction;
        Objects.requireNonNull(biFunction2);
        MenuType menuType = new MenuType((MenuType.MenuSupplier)biFunction2::apply, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS);
        pendingRegistrations.add(new Object[]{Registries.MENU, resourceLocation, (java.util.function.Supplier<MenuType>)() -> menuType});
        return menuType;
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> registerExtendedScreenHandler(ResourceLocation resourceLocation, EnvProxy.ExtendedClientScreenHandlerFactory<T> extendedClientScreenHandlerFactory) {
        EnvProxy.ExtendedClientScreenHandlerFactory<T> extendedClientScreenHandlerFactory2 = extendedClientScreenHandlerFactory;
        Objects.requireNonNull(extendedClientScreenHandlerFactory2);
        MenuType menuType = IMenuTypeExtension.create((IContainerFactory)(IContainerFactory)extendedClientScreenHandlerFactory2::create);
        pendingRegistrations.add(new Object[]{Registries.MENU, resourceLocation, (java.util.function.Supplier<MenuType>)() -> menuType});
        return menuType;
    }

    @Override
    public void registerItem(ResourceLocation resourceLocation, Item item) {
        pendingRegistrations.add(new Object[]{Registries.ITEM, resourceLocation, (java.util.function.Supplier<Item>)() -> item});
    }

    @Override
    public void registerEntity(ResourceLocation resourceLocation, EntityType<?> entityType) {
        pendingRegistrations.add(new Object[]{Registries.ENTITY_TYPE, resourceLocation, (java.util.function.Supplier<EntityType<?>>)() -> entityType});
    }

    /** 静态注册实体（供 Ic2Entities 等提前加载的类直接缓冲注册） */
    static void registerEntityDirect(ResourceLocation resourceLocation, EntityType<?> entityType) {
        pendingRegistrations.add(new Object[]{Registries.ENTITY_TYPE, resourceLocation, (java.util.function.Supplier<EntityType<?>>)() -> entityType});
    }

    @Override
    public WoodType registerSignType(String string) {
        return WoodType.register(new WoodType(string, net.minecraft.world.level.block.state.properties.BlockSetType.OAK));
    }

    @Override
    public void registerStatusEffect(ResourceLocation resourceLocation, MobEffect mobEffect) {
        pendingRegistrations.add(new Object[]{Registries.MOB_EFFECT, resourceLocation, (java.util.function.Supplier<MobEffect>)() -> mobEffect});
    }

    @Override
    public void registerFlammableBlock(Block block, int n, int n2) {
    }

    @Override
    public SoundEvent registerSoundEvent(String string) {
        ResourceLocation resourceLocation = IC2.getIdentifier(string);
        SoundEvent soundEvent = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(resourceLocation);
        pendingRegistrations.add(new Object[]{Registries.SOUND_EVENT, resourceLocation, (java.util.function.Supplier<SoundEvent>)() -> soundEvent});
        return soundEvent;
    }

    @Override
    public GameEvent registerGameEvent(String string, int n) {
        ResourceLocation resourceLocation = IC2.getIdentifier(string);
        GameEvent gameEvent = new GameEvent(n);
        pendingRegistrations.add(new Object[]{Registries.GAME_EVENT, resourceLocation, (java.util.function.Supplier<GameEvent>)() -> gameEvent});
        return gameEvent;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <FC extends FeatureConfiguration, F extends Feature<FC>> CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> registerConfiguredFeature(ResourceLocation resourceLocation, F f, FC FC) {
        pendingRegistrations.add(new Object[]{Registries.CONFIGURED_FEATURE, resourceLocation, (java.util.function.Supplier<ConfiguredFeature<FC, F>>)() -> new ConfiguredFeature<FC, F>(f, FC)});
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <FC extends FeatureConfiguration> void registerPlacedFeature(ResourceLocation resourceLocation, CompletableFuture<Holder<ConfiguredFeature<FC, ?>>> completableFuture, List<PlacementModifier> list) {
        pendingRegistrations.add(new Object[]{Registries.PLACED_FEATURE, resourceLocation, (java.util.function.Supplier<PlacedFeature>)() -> new PlacedFeature((Holder<ConfiguredFeature<?, ?>>)(Holder)completableFuture.join(), list)});
    }

    @Override
    public void attachPlacedFeatureToBiome(ResourceLocation resourceLocation, EnvProxy.BiomeSelector biomeSelector, GenerationStep.Decoration decoration) {
    }

    @Override
    public void registerPlacementModifierType(ResourceLocation resourceLocation, PlacementModifierType<?> placementModifierType) {
        pendingRegistrations.add(new Object[]{Registries.PLACEMENT_MODIFIER_TYPE, resourceLocation, (java.util.function.Supplier<PlacementModifierType<?>>)() -> placementModifierType});
    }

    @Override
    public <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(ResourceLocation resourceLocation, Codec<T> codec) {
        FoliagePlacerType foliagePlacerType = new FoliagePlacerType(codec.fieldOf("rubber_tree"));
        pendingRegistrations.add(new Object[]{Registries.FOLIAGE_PLACER_TYPE, resourceLocation, (java.util.function.Supplier<FoliagePlacerType>)() -> foliagePlacerType});
        return foliagePlacerType;
    }

    @Override
    public <T extends Recipe<?>> RecipeType<T> registerRecipeType(ResourceLocation resourceLocation) {
        RecipeType recipeType = RecipeType.simple((ResourceLocation)resourceLocation);
        pendingRegistrations.add(new Object[]{Registries.RECIPE_TYPE, resourceLocation, (java.util.function.Supplier<RecipeType>)() -> recipeType});
        return recipeType;
    }

    @Override
    public void registerRecipeSerializer(ResourceLocation resourceLocation, RecipeSerializer<?> recipeSerializer) {
        pendingRegistrations.add(new Object[]{Registries.RECIPE_SERIALIZER, resourceLocation, (java.util.function.Supplier<RecipeSerializer<?>>)() -> recipeSerializer});
    }

    @Override
    public void runAfterRegistryInit(Runnable runnable) {
        FmlMod.instance.runAfterRegistryInit(runnable);
    }

    @Override
    public CreativeModeTab createItemGroup(ResourceLocation resourceLocation, final Supplier<ItemStack> supplier) {
        // 1.21.1 的 CreativeModeTab 必须显式注册到 CREATIVE_MODE_TAB 注册表才会出现在创造模式物品栏。
        // 这里通过 pendingRegistrations 把 Supplier 加入缓冲，在 RegisterEvent 触发 CREATIVE_MODE_TAB 时 flush 进去。
        final ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
                net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, resourceLocation);
        final Supplier<CreativeModeTab> tabSupplier = () -> CreativeModeTab.builder()
                .title(net.minecraft.network.chat.Component.translatable(
                        String.format("itemGroup.%s.%s",
                                resourceLocation.getNamespace(), resourceLocation.getPath())))
                .icon(supplier)
                .displayItems((params, output) -> {
                    try {
                        for (Item item : BuiltInRegistries.ITEM) {
                            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("ic2")) {
                                // 第三十七轮：经典版（classic_*）物品与普通版**同名同图标**（上游就没给独立名称/贴图），
                                // 在创造栏里表现为"同一个 MFSU 出现两次"。1.12.2 的创造栏里没有它们 —— 详见
                                // Ic2CreativeVariants.HIDDEN_IN_CREATIVE_TAB 的注释。
                                if (Ic2CreativeVariants.isHiddenInCreativeTab(item)) {
                                    continue;
                                }
                                output.accept(item);
                                /*
                                 * 第三十三轮：补回 1.12.2 创造栏里的"满电版本"条目 ——
                                 * 四个电池类物品（充电电池/高级充电电池/能量水晶/兰波顿水晶）与
                                 * 四台储电机器（储电箱/CESU/MFE/MFSU）。1.21.1 的
                                 * Item.fillItemCategory 已被移除，只能在 displayItems 里追加。
                                 */
                                ItemStack fullVariant = Ic2CreativeVariants.getFullVariant(item);
                                if (!fullVariant.isEmpty()) {
                                    output.accept(fullVariant);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        // 注册尚未完成时跳过（实际不触发，displayItems 是 deferred）
                    }
                })
                .build();
        pendingRegistrations.add(new Object[]{
                net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                resourceLocation,
                (java.util.function.Supplier)tabSupplier
        });
        // 立即 build 出来一个实例，让 IC2.tabIC2 字段不为 null（注册后会变成"已注册"的实例，行为一致）
        return tabSupplier.get();
    }

    @Override
    public EnvFluidHandler createFluidStackHandler() {
        if (this.isClientEnv()) {
            try {
                return (EnvFluidHandler)Class.forName("ic2.forge.ClientEnvFluidHandlerForge").getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (ReflectiveOperationException reflectiveOperationException) {
                throw new RuntimeException(reflectiveOperationException);
            }
        }
        return new EnvFluidHandlerForge();
    }

    @Override
    public EnvItemHandler createItemHandler() {
        return new EnvItemHandlerForge();
    }

    @Override
    public float getBlastResistance(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Explosion explosion) {
        return blockState.getBlock().getExplosionResistance(blockState, blockGetter, blockPos, explosion);
    }

    @Override
    public BlockState rotate(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Rotation rotation) {
        return blockState.rotate(levelAccessor, blockPos, rotation);
    }

    @Override
    public boolean hasRecipeRemainder(ItemStack itemStack) {
        return itemStack.getItem().hasCraftingRemainingItem(itemStack);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack itemStack) {
        return itemStack.getItem().getCraftingRemainingItem(itemStack);
    }

    @Override
    public void registerBurnTime(ItemLike itemLike, int n) {
        burnTimeRecord.put(itemLike.asItem(), n);
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return itemStack.getBurnTime(null);
    }

    @Override
    public boolean biomeHasType(Holder<Biome> holder, EnvProxy.BiomeType biomeType) {
        return false;
    }

    @Override
    public Collection<EnvProxy.BiomeType> getBiomeTypes(Holder<Biome> holder) {
        return Collections.emptySet();
    }

    @Override
    public boolean openHandledScreen(Player player, MenuProvider menuProvider, GrowingBuffer growingBuffer) {
        ServerPlayer serverPlayer = (ServerPlayer)player;
        GrowingBuffer growingBuffer2 = growingBuffer;
        Objects.requireNonNull(growingBuffer2);
        serverPlayer.openMenu(menuProvider, growingBuffer2::writeTo);
        return true;
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public Player createFakePlayer(ServerLevel serverLevel, GameProfile gameProfile) {
        return FakePlayerFactory.get((ServerLevel)serverLevel, (GameProfile)gameProfile);
    }

    @Override
    public void announceProfileLoad(Set<String> set, String string) {
        NeoForge.EVENT_BUS.post((Event)new ProfileEvent.Load(set, string));
    }

    @Override
    public void announceProfileSwitch(String string, String string2) {
        NeoForge.EVENT_BUS.post((Event)new ProfileEvent.Switch(string, string2));
    }

    @Override
    public boolean announceRetexture(Level level, BlockPos blockPos, BlockState blockState, Direction direction, Player player, BlockState blockState2, String string, Direction direction2, int[] nArray) {
        RetextureEvent retextureEvent = new RetextureEvent(level, blockPos, blockState, direction, player, blockState2, string, direction2, nArray);
        NeoForge.EVENT_BUS.post((Event)retextureEvent);
        return retextureEvent.applied;
    }

    @Override
    public boolean announceExplosion(Level level, Entity entity, Vec3 vec3, double d, LivingEntity livingEntity, int n, double d2) {
        ExplosionEvent explosionEvent = new ExplosionEvent(level, entity, vec3, d, livingEntity, n, d2);
        return !NeoForge.EVENT_BUS.post(explosionEvent).isCanceled();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void flushPendingRegistrations(net.neoforged.neoforge.registries.RegisterEvent event) {
        java.util.Iterator<Object[]> it = pendingRegistrations.iterator();
        // 同一注册表内按 id 去重：部分物品（如 BaseElectricItem 系）会在构造器中
        // 通过 BlocksItems.registerItem 入队一次、又在 Ic2Items.register 入队第二次，
        // 导致 pendingRegistrations 出现同 key 重复条目——NeoForge 的
        // RegisterEvent.register 对重复 key 抛 "Adding duplicate key" ISE。
        // 此处仅注册首个条目，后续重复条目直接移除（语义等价于 Forge 时代的"重复注册=覆盖"）。
        java.util.Set<ResourceLocation> registeredIds = new java.util.HashSet<ResourceLocation>();
        while (it.hasNext()) {
            Object[] entry = it.next();
            ResourceKey<?> key = (ResourceKey<?>)entry[0];
            // 用 equals 而非 == ：防止 ResourceKey 字段常量被多次解析（CREATIVE_MODE_TAB 等）
            if (key.equals(event.getRegistryKey())) {
                ResourceLocation id = (ResourceLocation)entry[1];
                if (!registeredIds.add(id)) {
                    // 已在本轮 flush 注册过同 id 条目（重复入队），跳过
                    it.remove();
                    continue;
                }
                java.util.function.Supplier<?> supplier = (java.util.function.Supplier<?>)entry[2];
                event.register((ResourceKey)key, id, (java.util.function.Supplier)supplier);
                it.remove();
            }
        }
    }

    static int pendingRegistrationsSize() {
        return pendingRegistrations.size();
    }

}

