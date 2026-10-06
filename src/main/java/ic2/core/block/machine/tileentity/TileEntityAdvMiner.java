/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.BucketPickup
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.machine.tileentity;

import ic2.api.item.ElectricItem;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.FluidHandler;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.item.tool.ItemScanner;
import ic2.core.item.tool.ItemScannerAdv;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityAdvMiner
extends TileEntityElectricMachine
implements IHasGui,
INetworkClientTileEntityEventListener,
IUpgradableBlock {
    private int maxBlockScanCount;
    public final int defaultTier;
    public final int workTick;
    public boolean blacklist = true;
    public boolean silkTouch = false;
    public boolean redstonePowered = false;
    private final int scanEnergy = 64;
    private final int mineEnergy = 512;
    private BlockPos mineTarget;
    private short ticker = 0;
    public final InvSlotConsumableId scannerSlot = new InvSlotConsumableId(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, Ic2Items.SCANNER, Ic2Items.ADVANCED_SCANNER);
    public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
    public final InvSlot filterSlot = new InvSlot(this, "list", null, 15);
    protected final Redstone redstone;

    public TileEntityAdvMiner(BlockPos blockPos, BlockState blockState) {
        this(blockPos, blockState, Math.min(2 + ConfigUtil.getInt(MainConfig.get(), "balance/minerDischargeTier"), 5));
    }

    public TileEntityAdvMiner(BlockPos blockPos, BlockState blockState, int n) {
        super(Ic2BlockEntities.ADVANCED_MINER, blockPos, blockState, 4000000, n);
        this.defaultTier = n;
        this.workTick = 20;
        this.redstone = this.addComponent(new Redstone(this));
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getLevel().isClientSide) {
            this.setUpgradestat();
        }
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        if (compoundTag.contains("mineTargetX")) {
            this.mineTarget = new BlockPos(compoundTag.getInt("mineTargetX"), compoundTag.getInt("mineTargetY"), compoundTag.getInt("mineTargetZ"));
        }
        this.blacklist = compoundTag.getBoolean("blacklist");
        this.silkTouch = compoundTag.getBoolean("silkTouch");
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        if (this.mineTarget != null) {
            compoundTag.putInt("mineTargetX", this.mineTarget.getX());
            compoundTag.putInt("mineTargetY", this.mineTarget.getY());
            compoundTag.putInt("mineTargetZ", this.mineTarget.getZ());
        }
        compoundTag.putBoolean("blacklist", this.blacklist);
        compoundTag.putBoolean("silkTouch", this.silkTouch);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.getLevel().isClientSide) {
            this.setUpgradestat();
        }
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.chargeTool();
        if (this.work()) {
            super.setChanged();
            this.setActive(true);
        } else {
            this.setActive(false);
        }
    }

    private boolean work() {
        if (!this.energy.canUseEnergy(512.0)) {
            return false;
        }
        if (this.redstone.hasRedstoneInput()) {
            return false;
        }
        if (this.mineTarget != null && this.mineTarget.getY() < 0) {
            return false;
        }
        ItemStack itemStack = this.scannerSlot.get();
        if (StackUtil.isEmpty(itemStack) || !ElectricItem.manager.canUse(itemStack, 64.0)) {
            return false;
        }
        this.ticker = (short)(this.ticker + 1);
        if (this.ticker != this.workTick) {
            return true;
        }
        this.ticker = 0;
        int n = itemStack.getItem() instanceof ItemScannerAdv ? 32 : (itemStack.getItem() instanceof ItemScanner ? 16 : 0);
        if (this.mineTarget == null) {
            this.mineTarget = new BlockPos(this.worldPosition.getX() - n - 1, this.worldPosition.getY() - 1, this.worldPosition.getZ() - n);
            if (this.mineTarget.getY() < 0) {
                return false;
            }
        }
        int n2 = this.maxBlockScanCount;
        Level level = this.getLevel();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.mineTarget.getX(), this.mineTarget.getY(), this.mineTarget.getZ());
        do {
            if (mutableBlockPos.getX() < this.worldPosition.getX() + n) {
                mutableBlockPos = new BlockPos.MutableBlockPos(mutableBlockPos.getX() + 1, mutableBlockPos.getY(), mutableBlockPos.getZ());
            } else if (mutableBlockPos.getZ() < this.worldPosition.getZ() + n) {
                mutableBlockPos = new BlockPos.MutableBlockPos(this.worldPosition.getX() - n, mutableBlockPos.getY(), mutableBlockPos.getZ() + 1);
            } else {
                mutableBlockPos = new BlockPos.MutableBlockPos(this.worldPosition.getX() - n, mutableBlockPos.getY() - 1, this.worldPosition.getZ() - n);
                if (mutableBlockPos.getY() < 0) {
                    this.mineTarget = new BlockPos((Vec3i)mutableBlockPos);
                    return true;
                }
            }
            ElectricItem.manager.discharge(itemStack, 64.0, Integer.MAX_VALUE, true, false, false);
            BlockState blockState = level.getBlockState((BlockPos)mutableBlockPos);
            Block block = blockState.getBlock();
            if (!blockState.isAir() && this.canMine((BlockPos)mutableBlockPos, block, blockState)) {
                this.mineTarget = new BlockPos((Vec3i)mutableBlockPos);
                this.doMine(this.mineTarget, block, blockState);
                break;
            }
            this.mineTarget = new BlockPos((Vec3i)mutableBlockPos);
        } while (--n2 > 0 && ElectricItem.manager.canUse(itemStack, 64.0));
        return true;
    }

    private void chargeTool() {
        if (!this.scannerSlot.isEmpty()) {
            this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), this.energy.getSinkTier(), false, false));
        }
    }

    public void doMine(BlockPos blockPos, Block block, BlockState blockState) {
        Level level = this.getLevel();
        StackUtil.distributeDrops(this, new ArrayList<ItemStack>(StackUtil.getDrops((BlockGetter)level, blockPos, blockState, null, 0, this.silkTouch)));
        level.removeBlock(blockPos, false);
        this.energy.useEnergy(512.0);
    }

    public boolean canMine(BlockPos blockPos, Block block, BlockState blockState) {
        if (block instanceof BucketPickup || FluidHandler.getWorldFluid(blockState) != null) {
            return false;
        }
        Level level = this.getLevel();
        if (blockState.getDestroySpeed((BlockGetter)level, blockPos) < 0.0f) {
            return false;
        }
        List<ItemStack> list = StackUtil.getDrops((BlockGetter)level, blockPos, blockState, null, 0, this.silkTouch);
        if (list.isEmpty()) {
            return false;
        }
        if (block instanceof EntityBlock && OreValues.get(list) <= 0) {
            return false;
        }
        if (this.blacklist) {
            for (ItemStack itemStack : list) {
                for (ItemStack itemStack2 : this.filterSlot) {
                    if (!StackUtil.checkItemEquality(itemStack, itemStack2)) continue;
                    return false;
                }
            }
            return true;
        }
        for (ItemStack itemStack : list) {
            for (ItemStack itemStack3 : this.filterSlot) {
                if (!StackUtil.checkItemEquality(itemStack, itemStack3)) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNetworkEvent(Player player, int n) {
        switch (n) {
            case 0: {
                this.mineTarget = null;
                break;
            }
            case 1: {
                if (this.getActive()) break;
                this.blacklist = !this.blacklist;
                break;
            }
            case 2: {
                if (this.getActive()) break;
                this.silkTouch = !this.silkTouch;
            }
        }
    }

    public void setUpgradestat() {
        this.upgradeSlot.onChanged();
        int n = this.upgradeSlot.getTier(this.defaultTier);
        this.energy.setSinkTier(n);
        this.dischargeSlot.setTier(n);
        this.maxBlockScanCount = 5 * (this.upgradeSlot.augmentation + 1);
    }

    public ContainerBase<TileEntityAdvMiner> createServerScreenHandler(int n, Player player) {
        return new ContainerAdvMiner(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return new ContainerAdvMiner(n, inventory, this);
    }

    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }

    @Override
    public boolean useEnergy(double d) {
        return this.energy.useEnergy(d);
    }

    public BlockPos getMineTarget() {
        return this.mineTarget;
    }

    @Override
    public void onPlaced(ItemStack itemStack, LivingEntity livingEntity, Direction direction) {
        super.onPlaced(itemStack, livingEntity, direction);
        if (!this.getLevel().isClientSide) {
            CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
            this.energy.addEnergy(compoundTag.getDouble("energy"));
        }
    }

    @Override
    protected ItemStack adjustDrop(ItemStack itemStack, boolean bl) {
        double d;
        itemStack = super.adjustDrop(itemStack, bl);
        if ((bl || this.teBlock.getDefaultDrop() == Ic2TileEntityBlock.DefaultDrop.Self) && (d = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops")) > 0.0) {
            CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
            compoundTag.putDouble("energy", this.energy.getEnergy() * d);
        }
        return itemStack;
    }

    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Augmentable, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer);
    }
}

