/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.sound.Sound;
import ic2.core.util.LiquidUtil;
import ic2.core.util.PumpUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPump
extends TileEntityElectricMachine
implements IHasGui,
IUpgradableBlock,
IGuiValueProvider {
    public final int defaultTier;
    public int energyConsume = 1;
    public int operationsPerTick;
    public final int defaultEnergyStorage;
    public final int defaultEnergyConsume;
    public final int defaultOperationLength;
    private Sound sound;
    private TileEntityMiner miner = null;
    public boolean redstonePowered = false;
    public final InvSlotConsumableLiquid containerSlot = new InvSlotConsumableLiquid(this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill);
    public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
    @GuiSynced
    protected final Ic2FluidTank fluidTank;
    public short progress = 0;
    public int operationLength = 20;
    @GuiSynced
    public float guiProgress;
    protected final Fluids fluids;

    public TileEntityPump(BlockPos blockPos, BlockState blockState) {
        super(Ic2BlockEntities.PUMP, blockPos, blockState, 20, 1);
        this.defaultEnergyConsume = 1;
        this.defaultOperationLength = 20;
        this.defaultTier = 1;
        this.defaultEnergyStorage = 1 * this.operationLength;
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankExtract("fluid", 8000);
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!Objects.requireNonNull(this.getLevel()).isClientSide) {
            this.setUpgradestat();
        }
    }

    @Override
    protected void onUnloaded() {
        if (IC2.sideProxy.isRendering() && this.sound != null) {
            IC2.soundManager.removeSound(this, this.sound);
            this.sound = null;
        }
        this.miner = null;
        super.onUnloaded();
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.progress = compoundTag.getShort("progress");
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        compoundTag.putShort("progress", this.progress);
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean bl = false;
        if (this.canOperate() && this.energy.getEnergy() >= (double)(this.energyConsume * this.operationLength)) {
            if (this.progress < this.operationLength) {
                this.progress = (short)(this.progress + 1);
                this.energy.useEnergy(this.energyConsume);
            } else {
                this.progress = 0;
                this.operate(false);
            }
            this.activate();
        } else {
            this.shutdown(false);
        }
        bl |= this.containerSlot.processFromTank(this.fluidTank, this.outputSlot);
        this.guiProgress = (float)this.progress / (float)this.operationLength;
        if (bl |= this.upgradeSlot.tickNoMark()) {
            super.setChanged();
        }
    }

    public boolean canOperate() {
        return this.operate(true);
    }

    public boolean operate(boolean bl) {
        if (this.miner == null || this.miner.isRemoved()) {
            this.miner = null;
            Level level = this.getLevel();
            if (level == null) {
                return false;
            }
            for (Direction direction : Util.downSideFacings) {
                BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
                if (!(blockEntity instanceof TileEntityMiner)) continue;
                this.miner = (TileEntityMiner)blockEntity;
                break;
            }
        }
        Ic2FluidStack fluidStack = null;
        if (this.miner != null) {
            if (this.miner.canProvideLiquid) {
                fluidStack = this.pump(this.miner.liquidPos, bl, this.miner);
            }
        } else {
            Direction direction = this.getFacing();
            fluidStack = this.pump(this.worldPosition.relative(direction), bl, this.miner);
        }
        if (fluidStack != null && this.fluidTank.fillMbUnchecked(fluidStack, true) > 0) {
            if (!bl) {
                this.fluidTank.fillMbUnchecked(fluidStack, false);
            }
            return true;
        }
        return false;
    }

    public Ic2FluidStack pump(BlockPos blockPos, boolean bl, TileEntityMiner tileEntityMiner) {
        BlockEntity blockEntity;
        Level level = this.getLevel();
        if (level == null) {
            return null;
        }
        int n = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
        if (tileEntityMiner == null && n > 0) {
            Direction direction;
            blockEntity = level.getBlockEntity(blockPos);
            BlockState blockState = level.getBlockState(blockPos);
            if (LiquidUtil.isFluidTile(blockState, blockEntity, direction = this.getFacing().getOpposite())) {
                if (n > 1000) {
                    n = 1000;
                }
                return LiquidUtil.drainTile(blockState, level, blockPos, direction, n, bl);
            }
        }
        if (n >= 1000) {
            BlockPos sourcePos;
            if (tileEntityMiner != null && tileEntityMiner.canProvideLiquid) {
                assert (tileEntityMiner.liquidPos != null);
                sourcePos = tileEntityMiner.liquidPos;
            } else {
                sourcePos = PumpUtil.searchFluidSource(level, blockPos);
            }
            if (sourcePos != null) {
                return LiquidUtil.drainWorldFluidBlock(level, sourcePos, bl);
            }
        }
        return null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (IC2.sideProxy.isSimulating()) {
            this.setUpgradestat();
        }
    }

    public void setUpgradestat() {
        double d = (double)this.progress / (double)this.operationLength;
        this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
        this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
        this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
        this.energy.setSinkTier(this.upgradeSlot.getTier(this.defaultTier));
        this.dischargeSlot.setTier(this.energy.getSinkTier());
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
        this.progress = (short)Math.floor(d * (double)this.operationLength + 0.1);
    }

    @Override
    public double getGuiValue(String string) {
        if (string.equals("progress")) {
            return this.guiProgress;
        }
        throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + string);
    }

    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }

    @Override
    public boolean useEnergy(double d) {
        return this.energy.useEnergy(d);
    }

    @Override
    public ContainerBase<?> createServerScreenHandler(int n, Player player) {
        return DynamicContainer.create(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return DynamicContainer.create(n, inventory, this);
    }

    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, new UpgradableProperty[]{UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing});
    }
}

