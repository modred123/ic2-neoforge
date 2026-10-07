/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.PathNavigationRegion
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.logging.log4j.Level
 */
package ic2.core.block.reactor.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.reactor.IBaseReactorComponent;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.reactor.IReactorComponent;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2DamageSource;
import ic2.core.Ic2Explosion;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotReactor;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorRedstonePort;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.sound.Sound;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.WorldUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class TileEntityNuclearReactorElectric
extends TileEntityInventory
implements IHasGui,
IReactor,
IEnergySource,
IMetaDelegate,
IGuiValueProvider {
    public Sound soundMain;
    public Sound soundGeiger;
    private float lastOutput = 0.0f;
    public final Fluids.InternalFluidTank inputTank;
    public final Fluids.InternalFluidTank outputTank;
    private final List<IEnergyTile> subTiles = new ArrayList<IEnergyTile>();
    public final InvSlotReactor reactorSlot;
    public final InvSlotOutput coolantoutputSlot;
    public final InvSlotOutput hotcoolantoutputSlot;
    public final InvSlotConsumableLiquidByManager coolantinputSlot;
    public final InvSlotConsumableLiquidByTank hotcoolinputSlot;
    public final Redstone redstone;
    protected final Fluids fluids;
    public float output = 0.0f;
    public int updateTicker = IC2.random.nextInt(this.getTickRate());
    public int heat = 0;
    public int maxHeat = 10000;
    public float hem = 1.0f;
    private int EmitHeatbuffer = 0;
    public int EmitHeat = 0;
    private boolean fluidCooled = false;
    public boolean addedToEnergyNet = false;
    private static final float huOutputModifier = 40.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/FluidReactor/outputModifier");

    public TileEntityNuclearReactorElectric(BlockPos blockPos, BlockState blockState) {
        super((BlockEntityType<? extends TileEntityInventory>)Ic2BlockEntities.NUCLEAR_REACTOR, blockPos, blockState);
        this.fluids = this.addComponent(new Fluids(this));
        this.inputTank = this.fluids.addTank("inputTank", 10000, InvSlot.Access.NONE, InvSlot.InvSide.ANY, Fluids.fluidPredicate(Recipes.liquidHeatupManager));
        this.outputTank = this.fluids.addTank("outputTank", 10000, InvSlot.Access.NONE);
        this.reactorSlot = new InvSlotReactor(this, "reactor", 54);
        this.coolantinputSlot = new InvSlotConsumableLiquidByManager(this, "coolantinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidHeatupManager);
        this.hotcoolinputSlot = new InvSlotConsumableLiquidByTank(this, "hotcoolinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Fill, this.outputTank);
        this.coolantoutputSlot = new InvSlotOutput(this, "coolantoutputSlot", 1);
        this.hotcoolantoutputSlot = new InvSlotOutput(this, "hotcoolantoutputSlot", 1);
        this.redstone = this.addComponent(new Redstone(this));
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getLevel().isClientSide && !this.isFluidCooled()) {
            this.refreshChambers();
            EnergyNet.instance.addBlockEntityTile(this);
            this.addedToEnergyNet = true;
        }
        this.createChamberRedstoneLinks();
        if (this.isFluidCooled()) {
            this.createCasingRedstoneLinks();
            this.openTanks();
        }
    }

    @Override
    protected void onUnloaded() {
        if (IC2.sideProxy.isRendering()) {
            IC2.soundManager.removeAllSound(this);
            this.soundMain = null;
            this.soundGeiger = null;
        }
        if (IC2.sideProxy.isSimulating() && this.addedToEnergyNet) {
            EnergyNet.instance.removeTile(this);
            this.addedToEnergyNet = false;
        }
        super.onUnloaded();
    }

    public int gaugeHeatScaled(int n) {
        return n * this.heat / (this.maxHeat / 100 * 85);
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.heat = compoundTag.getInt("heat");
        this.output = compoundTag.getShort("output");
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        compoundTag.putInt("heat", this.heat);
        compoundTag.putShort("output", (short)this.getReactorEnergyOutput());
    }

    @Override
    protected void onNeighborChange(Block block, BlockPos blockPos) {
        super.onNeighborChange(block, blockPos);
        if (this.addedToEnergyNet) {
            this.refreshChambers();
        }
    }

    @Override
    public void drawEnergy(double d) {
    }

    @Override
    public boolean emitsEnergyTo(IEnergyAcceptor iEnergyAcceptor, Direction direction) {
        return true;
    }

    @Override
    public double getOfferedEnergy() {
        return this.getReactorEnergyOutput() * 5.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear");
    }

    @Override
    public int getSourceTier() {
        return 5;
    }

    @Override
    public double getReactorEUEnergyOutput() {
        return this.getOfferedEnergy();
    }

    @Override
    public List<IEnergyTile> getSubTiles() {
        return Collections.unmodifiableList(new ArrayList<IEnergyTile>(this.subTiles));
    }

    private void processfluidsSlots() {
        this.coolantinputSlot.processIntoTank(this.inputTank, this.coolantoutputSlot);
        this.hotcoolinputSlot.processFromTank(this.outputTank, this.hotcoolantoutputSlot);
    }

    public void refreshChambers() {
        Level level = this.getLevel();
        ArrayList<IEnergyTile> arrayList = new ArrayList<IEnergyTile>();
        arrayList.add(this);
        for (Direction direction : Util.ALL_DIRS) {
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
            if (!(blockEntity instanceof TileEntityReactorChamberElectric) || blockEntity.isRemoved()) continue;
            arrayList.add((TileEntityReactorChamberElectric)blockEntity);
        }
        if (!arrayList.equals(this.subTiles)) {
            if (this.addedToEnergyNet) {
                EnergyNet.instance.removeTile(this);
            }
            this.subTiles.clear();
            this.subTiles.addAll(arrayList);
            if (this.addedToEnergyNet) {
                EnergyNet.instance.addBlockEntityTile(this);
            }
        }
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.updateTicker++ % this.getTickRate() != 0) {
            return;
        }
        if (!Util.isAreaLoaded((LevelReader)this.getLevel(), this.worldPosition, 8)) {
            this.output = 0.0f;
        } else {
            boolean bl = this.isFluidReactor();
            if (this.fluidCooled != bl) {
                if (bl) {
                    this.enableFluidMode();
                } else {
                    this.disableFluidMode();
                }
                this.fluidCooled = bl;
            }
            this.dropAllUnfittingStuff();
            this.output = 0.0f;
            this.maxHeat = 10000;
            this.hem = 1.0f;
            this.processChambers();
            if (this.fluidCooled) {
                this.processfluidsSlots();
                Ic2FluidStack ic2FluidStack = this.inputTank.getFluidStack();
                assert (ic2FluidStack == null || Recipes.liquidHeatupManager.acceptsFluid(this.inputTank.getFluidStack().getFluid()));
                int n = (int)(huOutputModifier * (float)this.EmitHeatbuffer);
                int n2 = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
                this.EmitHeatbuffer = 0;
                if (n2 > 0 && ic2FluidStack != null) {
                    Ic2FluidStack ic2FluidStack2;
                    ILiquidHeatExchangerManager.HeatExchangeProperty heatExchangeProperty = Recipes.liquidHeatupManager.getHeatExchangeProperty(ic2FluidStack.getFluid());
                    int n3 = n / heatExchangeProperty.huPerMB;
                    if (n3 < n2) {
                        this.EmitHeatbuffer = (int)((float)(n % heatExchangeProperty.huPerMB) / huOutputModifier);
                        this.EmitHeat = (int)((float)n / huOutputModifier);
                        ic2FluidStack2 = this.inputTank.drainMbUnchecked(n3, true);
                    } else {
                        this.EmitHeat = n2 * heatExchangeProperty.huPerMB;
                        ic2FluidStack2 = this.inputTank.drainMbUnchecked(n2, true);
                    }
                    if (ic2FluidStack2 != null) {
                        this.EmitHeat = ic2FluidStack2.getAmountMb() * heatExchangeProperty.huPerMB;
                        n -= this.inputTank.drainMbUnchecked(ic2FluidStack2.getAmountMb(), false).getAmountMb() * heatExchangeProperty.huPerMB;
                        this.outputTank.fillMbUnchecked(Ic2FluidStack.create(heatExchangeProperty.outputFluid, ic2FluidStack2.getAmountMb()), false);
                    } else {
                        this.EmitHeat = 0;
                    }
                } else {
                    this.EmitHeat = 0;
                }
                this.addHeat((int)((float)n / huOutputModifier));
            }
            if (this.calculateHeatEffects()) {
                return;
            }
            this.setActive(this.heat >= 1000 || this.output > 0.0f);
            this.setChanged();
        }
        IC2.network.get(true).updateTileEntityField(this, "output");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        TileEntityNuclearReactorElectric.showHeatEffects(this.getLevel(), this.worldPosition, this.heat);
    }

    public static void showHeatEffects(Level level, BlockPos blockPos, int n) {
        RandomSource randomSource = level.random;
        if (randomSource.nextInt(8) != 0) {
            return;
        }
        int n2 = n / 1000;
        if (n2 > 0) {
            int n3;
            n2 = randomSource.nextInt(n2);
            for (n3 = 0; n3 < n2; ++n3) {
                level.addParticle((ParticleOptions)ParticleTypes.SMOKE, (double)((float)blockPos.getX() + randomSource.nextFloat()), (double)((float)blockPos.getY() + 0.95f), (double)((float)blockPos.getZ() + randomSource.nextFloat()), 0.0, 0.0, 0.0);
            }
            n2 -= randomSource.nextInt(4) + 3;
            for (n3 = 0; n3 < n2; ++n3) {
                level.addParticle((ParticleOptions)ParticleTypes.FLAME, (double)((float)blockPos.getX() + randomSource.nextFloat()), (double)(blockPos.getY() + 1), (double)((float)blockPos.getZ() + randomSource.nextFloat()), 0.0, 0.0, 0.0);
            }
        }
    }

    public void dropAllUnfittingStuff() {
        ItemStack itemStack;
        int n;
        for (n = 0; n < this.reactorSlot.size(); ++n) {
            itemStack = this.reactorSlot.get(n);
            if (itemStack == null || this.isUsefulItem(itemStack, false)) continue;
            this.reactorSlot.put(n, null);
            this.eject(itemStack);
        }
        for (n = this.reactorSlot.size(); n < this.reactorSlot.rawSize(); ++n) {
            itemStack = this.reactorSlot.get(n);
            this.reactorSlot.put(n, null);
            this.eject(itemStack);
        }
    }

    public boolean isUsefulItem(ItemStack itemStack, boolean bl) {
        Item item = itemStack.getItem();
        if (item == null) {
            return false;
        }
        if (bl && this.fluidCooled && item.getClass() == ItemReactorHeatStorage.class && itemStack.getDamageValue() > 0) {
            return false;
        }
        return item instanceof IBaseReactorComponent && (!bl || ((IBaseReactorComponent)item).canBePlacedIn(itemStack, this));
    }

    public void eject(ItemStack itemStack) {
        if (!IC2.sideProxy.isSimulating() || itemStack == null) {
            return;
        }
        StackUtil.dropAsEntity(this.getLevel(), this.worldPosition, itemStack);
    }

    public boolean calculateHeatEffects() {
        if (this.heat < 4000 || !IC2.sideProxy.isSimulating() || ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit") <= 0.0f) {
            return false;
        }
        float f = (float)this.heat / (float)this.maxHeat;
        if (f >= 1.0f) {
            this.explode();
            return true;
        }
        Level level = this.getLevel();
        if (f >= 0.85f && level.random.nextFloat() <= 0.2f * this.hem) {
            BlockPos pos = this.getRandCoord(2);
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isAir()) {
                level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
            } else if (blockState.getDestroySpeed(level, pos) >= 0.0f && level.getBlockEntity(pos) == null) {
                MapColor material = blockState.getMapColor(level, pos);
                if (material == MapColor.STONE || material == MapColor.METAL || material == MapColor.FIRE || material == MapColor.DIRT || material == MapColor.CLAY) {
                    level.setBlockAndUpdate(pos, net.minecraft.world.level.material.Fluids.FLOWING_LAVA.defaultFluidState().createLegacyBlock());
                } else {
                    level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                }
            }
        }
        if (f >= 0.7f) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB((double)(this.worldPosition.getX() - 3), (double)(this.worldPosition.getY() - 3), (double)(this.worldPosition.getZ() - 3), (double)(this.worldPosition.getX() + 4), (double)(this.worldPosition.getY() + 4), (double)(this.worldPosition.getZ() + 4)), net.minecraft.world.entity.EntitySelector.NO_SPECTATORS)) {
            // 第五十轮修复（P0 崩溃）：第三参数原为 `null` —— 1.12.2 的三参
            // `getEntitiesWithinAABB(Class, AABB)` 没有谓词，迁移补位时补成了 null；
            // 而 1.21.1 的 `Level.getEntities(...)` 会直接 `predicate.test(...)` → NullPointerException
            // （crash-2026-10-07_20.30.18：过热反应堆 calculateHeatEffects:425 每 tick 崩服）。
            // 改用 `EntitySelector.NO_SPECTATORS`：与 1.12.2 "命中范围内所有实体"语义一致，只是排除旁观者。
                entity.hurt(Ic2DamageSource.radiation(level), (float)((int)((float)level.random.nextInt(4) * this.hem)));
            }
        }
        if (f >= 0.5f && level.random.nextFloat() <= this.hem) {
            BlockPos pos2 = this.getRandCoord(2);
            if (level.getBlockState(pos2).getFluidState().is(net.minecraft.world.level.material.Fluids.WATER)) {
                level.removeBlock(pos2, false);
            }
        }
        if (f >= 0.4f && level.random.nextFloat() <= this.hem) {
            BlockPos pos3 = this.getRandCoord(2);
            if (level.getBlockEntity(pos3) == null) {
                MapColor mapColor = level.getBlockState(pos3).getMapColor(level, pos3);
                if (mapColor == MapColor.WOOD || mapColor == MapColor.PLANT || mapColor == MapColor.WOOL) {
                    level.setBlockAndUpdate(pos3, Blocks.FIRE.defaultBlockState());
                }
            }
        }
        return false;
    }

    public BlockPos getRandCoord(int n) {
        BlockPos blockPos;
        if (n <= 0) {
            return null;
        }
        Level level = this.getLevel();
        while ((blockPos = this.worldPosition.offset(level.random.nextInt(2 * n + 1) - n, level.random.nextInt(2 * n + 1) - n, level.random.nextInt(2 * n + 1) - n)).equals((Object)this.worldPosition)) {
        }
        return blockPos;
    }

    public void processChambers() {
        int n = this.getReactorSize();
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 6; ++j) {
                for (int k = 0; k < n; ++k) {
                    ItemStack itemStack = this.reactorSlot.get(k, j);
                    if (itemStack == null || !(itemStack.getItem() instanceof IReactorComponent)) continue;
                    IReactorComponent iReactorComponent = (IReactorComponent)itemStack.getItem();
                    iReactorComponent.processChamber(itemStack, this, k, j, i == 0);
                }
            }
        }
    }

    @Override
    public boolean produceEnergy() {
        return this.redstone.hasRedstoneInput() && ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear") > 0.0f;
    }

    public int getReactorSize() {
        Level level = this.getLevel();
        if (level == null) {
            return 9;
        }
        int n = 3;
        for (Direction direction : Util.ALL_DIRS) {
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
            if (!(blockEntity instanceof TileEntityReactorChamberElectric)) continue;
            ++n;
        }
        return n;
    }

    private boolean isFullSize() {
        return this.getReactorSize() == 9;
    }

    @Override
    public int getTickRate() {
        return 20;
    }

    @Override
    protected InteractionResult onActivated(Player player, InteractionHand interactionHand, Direction direction, Vec3 vec3) {
        if (StackUtil.checkItemEquality(StackUtil.get(player, interactionHand), new ItemStack((ItemLike)Ic2Items.REACTOR_CHAMBER))) {
            return InteractionResult.PASS;
        }
        return super.onActivated(player, interactionHand, direction, vec3);
    }

    @Override
    public ContainerBase<?> createServerScreenHandler(int n, Player player) {
        return new ContainerNuclearReactor(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return new ContainerNuclearReactor(n, inventory, this);
    }

    @Override
    public void onNetworkUpdate(String string) {
        if (string.equals("output")) {
            if (this.output > 0.0f) {
                if (this.lastOutput <= 0.0f) {
                    if (this.soundMain == null) {
                        this.soundMain = IC2.soundManager.createSound((Object)this, Ic2SoundEvents.GENERATOR_NUCLEAR_LOOP, SoundSource.BLOCKS, this.getBlockPos(), 1.0f, 1.0f);
                    }
                    if (this.soundMain != null) {
                        this.soundMain.play();
                    }
                }
                if (this.output < 40.0f) {
                    if (this.lastOutput <= 0.0f || this.lastOutput >= 40.0f) {
                        if (this.soundGeiger != null) {
                            IC2.soundManager.removeSound(this, this.soundGeiger);
                        }
                        this.soundGeiger = IC2.soundManager.createSound((Object)this, Ic2SoundEvents.GENERATOR_NUCLEAR_LOW_POWER, SoundSource.BLOCKS, this.getBlockPos(), 1.0f, 1.0f);
                        if (this.soundGeiger != null) {
                            this.soundGeiger.play();
                        }
                    }
                } else if (this.output < 80.0f) {
                    if (this.lastOutput < 40.0f || this.lastOutput >= 80.0f) {
                        if (this.soundGeiger != null) {
                            IC2.soundManager.removeSound(this, this.soundGeiger);
                        }
                        this.soundGeiger = IC2.soundManager.createSound((Object)this, Ic2SoundEvents.GENERATOR_NUCLEAR_MEDIUM_POWER, SoundSource.BLOCKS, this.getBlockPos(), 1.0f, 1.0f);
                        if (this.soundGeiger != null) {
                            this.soundGeiger.play();
                        }
                    }
                } else if (this.output >= 80.0f && this.lastOutput < 80.0f) {
                    if (this.soundGeiger != null) {
                        IC2.soundManager.removeSound(this, this.soundGeiger);
                    }
                    this.soundGeiger = IC2.soundManager.createSound((Object)this, Ic2SoundEvents.GENERATOR_NUCLEAR_HIGH_POWER, SoundSource.BLOCKS, this.getBlockPos(), 1.0f, 1.0f);
                    if (this.soundGeiger != null) {
                        this.soundGeiger.play();
                    }
                }
            } else if (this.lastOutput > 0.0f) {
                if (this.soundMain != null) {
                    this.soundMain.stop();
                }
                if (this.soundGeiger != null) {
                    this.soundGeiger.stop();
                }
            }
            this.lastOutput = this.output;
        }
        super.onNetworkUpdate(string);
    }

    @Override
    public BlockEntity getCoreTe() {
        return this;
    }

    @Override
    public BlockPos getPosition() {
        return this.worldPosition;
    }

    @Override
    public Level getWorldObj() {
        return this.getLevel();
    }

    @Override
    public int getHeat() {
        return this.heat;
    }

    @Override
    public void setHeat(int n) {
        this.heat = n;
    }

    @Override
    public int addHeat(int n) {
        this.heat += n;
        return this.heat;
    }

    @Override
    public ItemStack getItemAt(int n, int n2) {
        if (n < 0 || n >= this.getReactorSize() || n2 < 0 || n2 >= 6) {
            return null;
        }
        return this.reactorSlot.get(n, n2);
    }

    @Override
    public void setItemAt(int n, int n2, ItemStack itemStack) {
        if (n < 0 || n >= this.getReactorSize() || n2 < 0 || n2 >= 6) {
            return;
        }
        this.reactorSlot.put(n, n2, itemStack);
    }

    @Override
    public void explode() {
        float f = 10.0f;
        float f2 = 1.0f;
        for (int i = 0; i < this.reactorSlot.size(); ++i) {
            ItemStack itemStack = this.reactorSlot.get(i);
            if (!StackUtil.isEmpty(itemStack) && itemStack.getItem() instanceof IReactorComponent) {
                float f3 = ((IReactorComponent)itemStack.getItem()).influenceExplosion(itemStack, this);
                if (f3 > 0.0f && f3 < 1.0f) {
                    f2 *= f3;
                } else {
                    f += f3;
                }
            }
            this.reactorSlot.put(i, null);
        }
        IC2.log.log(LogCategory.PlayerActivity, org.apache.logging.log4j.Level.INFO, "Nuclear Reactor at %s melted (raw explosion power %f)", Util.formatPosition(this), Float.valueOf(f *= this.hem * f2));
        f = Math.min(f, ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit"));
        Level level = this.getLevel();
        for (Direction direction : Util.ALL_DIRS) {
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
            if (!(blockEntity instanceof TileEntityReactorChamberElectric)) continue;
            level.removeBlock(blockEntity.getBlockPos(), false);
        }
        level.removeBlock(this.worldPosition, false);
        Ic2Explosion ic2Explosion = new Ic2Explosion(level, null, this.worldPosition, f, 0.01f, Ic2Explosion.Type.Nuclear);
        ic2Explosion.doExplosion();
        // 第五十二轮：成就「让核反应堆熔毁」改为**真正爆炸时**授予（原先 criterion 是
        // `inventory_changed`，拿一个反应堆舱就会解锁，语义完全不对）。
        // 做法对齐 1.12.2 `EnergyCalculatorLeg.java:576-579`（过压炸机器成就）：取 20 格内最近的玩家。
        net.minecraft.world.entity.player.Player player = level.getNearestPlayer(
                (double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5,
                (double)this.worldPosition.getZ() + 0.5, 20.0, false);
        if (player != null) {
            IC2.achievements.issueAchievement(player, "makeNuclearReactorExplode");
        }
    }

    @Override
    public void addEmitHeat(int n) {
        this.EmitHeatbuffer += n;
    }

    @Override
    public int getMaxHeat() {
        return this.maxHeat;
    }

    @Override
    public void setMaxHeat(int n) {
        this.maxHeat = n;
    }

    @Override
    public float getHeatEffectModifier() {
        return this.hem;
    }

    @Override
    public void setHeatEffectModifier(float f) {
        this.hem = f;
    }

    @Override
    public float getReactorEnergyOutput() {
        return this.output;
    }

    @Override
    public float addOutput(float f) {
        return this.output += f;
    }

    @Override
    public boolean isFluidCooled() {
        return this.fluidCooled;
    }

    private void createChamberRedstoneLinks() {
        Level level = this.getLevel();
        for (Direction direction : Util.ALL_DIRS) {
            BlockPos blockPos = this.worldPosition.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (!(blockEntity instanceof TileEntityReactorChamberElectric)) continue;
            TileEntityReactorChamberElectric tileEntityReactorChamberElectric = (TileEntityReactorChamberElectric)blockEntity;
            if (tileEntityReactorChamberElectric.redstone.isLinked() && tileEntityReactorChamberElectric.redstone.getLinkReceiver() != this.redstone) {
                tileEntityReactorChamberElectric.destoryChamber(true);
                continue;
            }
            tileEntityReactorChamberElectric.redstone.linkTo(this.redstone);
        }
    }

    private void createCasingRedstoneLinks() {
        WorldUtil.findTileEntities(this.getLevel(), this.worldPosition, 2, new WorldUtil.ITileEntityResultHandler(){

            @Override
            public boolean onMatch(BlockEntity blockEntity) {
                if (blockEntity instanceof TileEntityReactorRedstonePort) {
                    ((TileEntityReactorRedstonePort)blockEntity).redstone.linkTo(TileEntityNuclearReactorElectric.this.redstone);
                }
                return false;
            }
        });
    }

    private void removeCasingRedstoneLinks() {
        for (Redstone redstone : this.redstone.getLinkedOrigins()) {
            if (!(redstone.getParent() instanceof TileEntityReactorRedstonePort)) continue;
            redstone.unlinkOutbound();
        }
    }

    private void enableFluidMode() {
        if (this.addedToEnergyNet) {
            EnergyNet.instance.removeTile(this);
            this.addedToEnergyNet = false;
        }
        this.createCasingRedstoneLinks();
        this.openTanks();
    }

    private void disableFluidMode() {
        if (!this.addedToEnergyNet) {
            this.refreshChambers();
            EnergyNet.instance.addBlockEntityTile(this);
            this.addedToEnergyNet = true;
        }
        this.removeCasingRedstoneLinks();
        this.closeTanks();
    }

    private void openTanks() {
        this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.I, InvSlot.InvSide.ANY);
        this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.O, InvSlot.InvSide.ANY);
    }

    private void closeTanks() {
        this.fluids.changeConnectivity(this.inputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
        this.fluids.changeConnectivity(this.outputTank, InvSlot.Access.NONE, InvSlot.InvSide.ANY);
    }

    private boolean isFluidReactor() {
        if (!this.isFullSize()) {
            return false;
        }
        if (!this.hasFluidChamber()) {
            return false;
        }
        final MutableBoolean mutableBoolean = new MutableBoolean();
        WorldUtil.findTileEntities(this.getLevel(), this.worldPosition, 4, new WorldUtil.ITileEntityResultHandler(){

            @Override
            public boolean onMatch(BlockEntity blockEntity) {
                if (!(blockEntity instanceof TileEntityNuclearReactorElectric)) {
                    return false;
                }
                if (blockEntity == TileEntityNuclearReactorElectric.this) {
                    return false;
                }
                TileEntityNuclearReactorElectric tileEntityNuclearReactorElectric = (TileEntityNuclearReactorElectric)blockEntity;
                if (tileEntityNuclearReactorElectric.isFullSize() && tileEntityNuclearReactorElectric.hasFluidChamber()) {
                    mutableBoolean.setTrue();
                    return true;
                }
                return false;
            }
        });
        return mutableBoolean.getValue() == false;
    }

    private boolean hasFluidChamber() {
        int n;
        int n2;
        int n3;
        int n4;
        PathNavigationRegion pathNavigationRegion = new PathNavigationRegion(this.getLevel(), this.worldPosition.offset(-2, -2, -2), this.worldPosition.offset(2, 2, 2));
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (n4 = 0; n4 < 2; ++n4) {
            n3 = this.worldPosition.getY() + 2 * (n4 * 2 - 1);
            for (n2 = this.worldPosition.getZ() - 2; n2 <= this.worldPosition.getZ() + 2; ++n2) {
                for (n = this.worldPosition.getX() - 2; n <= this.worldPosition.getX() + 2; ++n) {
                    mutableBlockPos.set(n, n3, n2);
                    if (TileEntityNuclearReactorElectric.isFluidChamberBlock((BlockGetter)pathNavigationRegion, (BlockPos)mutableBlockPos)) continue;
                    return false;
                }
            }
        }
        for (n4 = 0; n4 < 2; ++n4) {
            n3 = this.worldPosition.getZ() + 2 * (n4 * 2 - 1);
            for (n2 = this.worldPosition.getY() - 2 + 1; n2 <= this.worldPosition.getY() + 2 - 1; ++n2) {
                for (n = this.worldPosition.getX() - 2; n <= this.worldPosition.getX() + 2; ++n) {
                    mutableBlockPos.set(n, n2, n3);
                    if (TileEntityNuclearReactorElectric.isFluidChamberBlock((BlockGetter)pathNavigationRegion, (BlockPos)mutableBlockPos)) continue;
                    return false;
                }
            }
        }
        for (n4 = 0; n4 < 2; ++n4) {
            n3 = this.worldPosition.getX() + 2 * (n4 * 2 - 1);
            for (n2 = this.worldPosition.getY() - 2 + 1; n2 <= this.worldPosition.getY() + 2 - 1; ++n2) {
                for (n = this.worldPosition.getZ() - 2 + 1; n <= this.worldPosition.getZ() + 2 - 1; ++n) {
                    mutableBlockPos.set(n3, n2, n);
                    if (TileEntityNuclearReactorElectric.isFluidChamberBlock((BlockGetter)pathNavigationRegion, (BlockPos)mutableBlockPos)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isFluidChamberBlock(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (blockState.getBlock() == Ic2Blocks.REACTOR_VESSEL) {
            return true;
        }
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity == null) {
            return false;
        }
        return blockEntity instanceof IReactorChamber && ((IReactorChamber)blockEntity).isWall();
    }

    @Override
    public double getGuiValue(String string) {
        if ("heat".equals(string)) {
            return this.maxHeat == 0 ? 0.0 : (double)this.heat / (double)this.maxHeat;
        }
        throw new IllegalArgumentException("Invalid value: " + string);
    }

    public int gaugeLiquidScaled(int n, int n2) {
        switch (n2) {
            case 0: {
                if (this.inputTank.getFluidAmount() <= 0) {
                    return 0;
                }
                return this.inputTank.getFluidAmount() * n / this.inputTank.getCapacity();
            }
            case 1: {
                if (this.outputTank.getFluidAmount() <= 0) {
                    return 0;
                }
                return this.outputTank.getFluidAmount() * n / this.outputTank.getCapacity();
            }
        }
        return 0;
    }

    public Ic2FluidTank getinputtank() {
        return this.inputTank;
    }

    public Ic2FluidTank getoutputtank() {
        return this.outputTank;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

