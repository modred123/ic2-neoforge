/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.item.context.DirectionalPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.machine.tileentity;

import ic2.api.item.ElectricItem;
import ic2.api.item.IMiningDrill;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Player;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableBlock;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.block.machine.tileentity.TileEntityPump;
import ic2.core.fluid.FluidHandler;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.item.tool.ItemScanner;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityMiner
extends TileEntityElectricMachine
implements IHasGui,
IUpgradableBlock {
    private Mode lastMode = Mode.None;
    public int progress = 0;
    private int scannedLevel = -1;
    private int scanRange = 0;
    private int lastX;
    private int lastZ;
    public boolean pumpMode = false;
    public boolean canProvideLiquid = false;
    public BlockPos liquidPos;
    private AudioSource audioSource;
    public final InvSlot buffer;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotConsumable drillSlot = new InvSlotConsumableClass(this, "drill", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, IMiningDrill.class){

        @Override
        public boolean canOutput() {
            return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
        }
    };
    public final InvSlotConsumable pipeSlot = new InvSlotConsumableBlock(this, "pipe", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP){

        @Override
        public boolean canOutput() {
            return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
        }
    };
    public final InvSlotConsumable scannerSlot = new InvSlotConsumableId(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, new Item[]{Ic2Items.SCANNER, Ic2Items.ADVANCED_SCANNER}){

        @Override
        public boolean canOutput() {
            return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
        }
    };
    boolean tickingUpgrades = false;

    public TileEntityMiner(BlockPos blockPos, BlockState blockState) {
        super(Ic2BlockEntities.MINER, blockPos, blockState, 1000, ConfigUtil.getInt(MainConfig.get(), "balance/minerDischargeTier"), false);
        this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
        this.buffer = new InvSlot(this, "buffer", InvSlot.Access.IO, 15, InvSlot.InvSide.SIDE);
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.scannedLevel = -1;
        this.lastX = this.worldPosition.getX();
        this.lastZ = this.worldPosition.getZ();
        this.canProvideLiquid = false;
    }

    @Override
    protected void onUnloaded() {
        if (IC2.sideProxy.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }
        super.onUnloaded();
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.lastMode = Mode.values()[compoundTag.getInt("lastMode")];
        this.progress = compoundTag.getInt("progress");
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        compoundTag.putInt("lastMode", this.lastMode.ordinal());
        compoundTag.putInt("progress", this.progress);
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.chargeTools();
        this.tickingUpgrades = true;
        this.upgradeSlot.tick();
        this.tickingUpgrades = false;
        if (this.work()) {
            this.setChanged();
            this.setActive(true);
        } else {
            this.setActive(false);
        }
    }

    private void chargeTools() {
        if (!this.scannerSlot.isEmpty()) {
            this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), 2, false, false));
        }
        if (!this.drillSlot.isEmpty()) {
            this.energy.useEnergy(ElectricItem.manager.charge(this.drillSlot.get(), this.energy.getEnergy(), 3, false, false));
        }
    }

    private boolean work() {
        BlockPos.MutableBlockPos mutableBlockPos = this.getOperationPos();
        if (this.drillSlot.isEmpty()) {
            return this.withDrawPipe(mutableBlockPos);
        }
        if (mutableBlockPos.getY() >= this.getLevel().getMinBuildHeight()) {
            Level level = this.getLevel();
            BlockState blockState = level.getBlockState((BlockPos)mutableBlockPos);
            if (blockState.getBlock() != Ic2Blocks.MINING_PIPE_TIP) {
                if (mutableBlockPos.getY() > level.getMinBuildHeight()) {
                    return this.digDown(mutableBlockPos, blockState, false);
                }
                return false;
            }
            MineResult mineResult = this.mineLevel(mutableBlockPos.getY());
            if (mineResult == MineResult.Done) {
                mutableBlockPos.move(Direction.DOWN);
                blockState = level.getBlockState((BlockPos)mutableBlockPos);
                return this.digDown(mutableBlockPos, blockState, true);
            }
            return mineResult == MineResult.Working;
        }
        return false;
    }

    private BlockPos.MutableBlockPos getOperationPos() {
        BlockPos.MutableBlockPos mutableBlockPos = this.worldPosition.mutable().move(Direction.DOWN);
        Level level = this.getLevel();
        int n = level.getMinBuildHeight();
        BlockState blockState = Ic2Blocks.MINING_PIPE.defaultBlockState();
        while (mutableBlockPos.getY() >= n) {
            BlockState blockState2 = level.getBlockState((BlockPos)mutableBlockPos);
            if (blockState2 != blockState) {
                return mutableBlockPos;
            }
            mutableBlockPos.move(Direction.DOWN);
        }
        return mutableBlockPos;
    }

    private boolean withDrawPipe(BlockPos.MutableBlockPos mutableBlockPos) {
        if (this.lastMode != Mode.Withdraw) {
            this.lastMode = Mode.Withdraw;
            this.progress = 0;
        }
        if (mutableBlockPos.getY() < this.getLevel().getMinBuildHeight() || this.getLevel().getBlockState((BlockPos)mutableBlockPos).getBlock() != Ic2Blocks.MINING_PIPE_TIP) {
            mutableBlockPos.move(Direction.UP);
        }
        if (mutableBlockPos.getY() != this.worldPosition.getY() && this.energy.getEnergy() >= 3.0) {
            if (this.progress < 20) {
                this.energy.useEnergy(3.0);
                ++this.progress;
            } else {
                this.progress = 0;
                this.removePipe(mutableBlockPos);
            }
            return true;
        }
        return false;
    }

    private void removePipe(BlockPos.MutableBlockPos mutableBlockPos) {
        ItemStack itemStack;
        Item item;
        Level level = this.getLevel();
        level.removeBlock((BlockPos)mutableBlockPos, false);
        this.storeDrop(new ItemStack((ItemLike)Ic2Items.MINING_PIPE));
        ItemStack itemStack2 = this.pipeSlot.consume(1, true, false);
        if (itemStack2 != null && itemStack2.getItem() != Ic2Items.MINING_PIPE && (item = (itemStack = this.pipeSlot.consume(1)).getItem()) instanceof BlockItem) {
            ((BlockItem)item).place((BlockPlaceContext)new DirectionalPlaceContext(level, mutableBlockPos.above(), Direction.DOWN, itemStack, Direction.UP));
        }
    }

    private boolean digDown(BlockPos.MutableBlockPos mutableBlockPos, BlockState blockState, boolean bl) {
        ItemStack itemStack = this.pipeSlot.consume(1, true, false);
        if (itemStack == null || itemStack.getItem() != Ic2Items.MINING_PIPE) {
            return false;
        }
        if (mutableBlockPos.getY() < this.getLevel().getMinBuildHeight()) {
            if (bl) {
                this.getLevel().setBlockAndUpdate((BlockPos)mutableBlockPos.setY(this.getLevel().getMinBuildHeight()), Ic2Blocks.MINING_PIPE.defaultBlockState());
            }
            return false;
        }
        MineResult mineResult = this.mineBlock((BlockPos)mutableBlockPos, blockState);
        if (mineResult == MineResult.Failed_Temp || mineResult == MineResult.Failed_Perm) {
            if (bl) {
                this.getLevel().setBlockAndUpdate((BlockPos)mutableBlockPos.move(Direction.UP), Ic2Blocks.MINING_PIPE.defaultBlockState());
            }
            return false;
        }
        if (mineResult == MineResult.Done) {
            if (bl) {
                this.getLevel().setBlockAndUpdate(mutableBlockPos.above(), Ic2Blocks.MINING_PIPE.defaultBlockState());
            }
            this.pipeSlot.consume(1);
            this.getLevel().setBlockAndUpdate((BlockPos)mutableBlockPos, Ic2Blocks.MINING_PIPE_TIP.defaultBlockState());
        }
        return true;
    }

    private MineResult mineLevel(int n) {
        if (this.scannerSlot.isEmpty()) {
            return MineResult.Done;
        }
        if (this.scannedLevel != n) {
            this.scanRange = ((ItemScanner)this.scannerSlot.get().getItem()).startLayerScan(this.scannerSlot.get());
        }
        if (this.scanRange > 0) {
            this.scannedLevel = n;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            Level level = this.getLevel();
            Player player = Ic2Player.get(level);
            for (int i = this.worldPosition.getX() - this.scanRange; i <= this.worldPosition.getX() + this.scanRange; ++i) {
                for (int j = this.worldPosition.getZ() - this.scanRange; j <= this.worldPosition.getZ() + this.scanRange; ++j) {
                    mutableBlockPos.set(i, n, j);
                    BlockState blockState = level.getBlockState((BlockPos)mutableBlockPos);
                    boolean bl = false;
                    if ((blockState.is(Ic2BlockTags.ORES) || OreValues.get(StackUtil.getDrops((BlockGetter)level, (BlockPos)mutableBlockPos, blockState, 0)) > 0 || OreValues.get(StackUtil.getPickStack(level, (BlockPos)mutableBlockPos, blockState, player)) > 0) && this.canMine((BlockPos)mutableBlockPos, blockState)) {
                        bl = true;
                    } else if (this.pumpMode && LiquidUtil.getLiquid(level, (BlockPos)mutableBlockPos) != null && this.canPump((BlockPos)mutableBlockPos)) {
                        bl = true;
                    }
                    if (!bl) continue;
                    MineResult mineResult = this.mineTowards((BlockPos)mutableBlockPos);
                    if (mineResult == MineResult.Done) {
                        return MineResult.Working;
                    }
                    if (mineResult == MineResult.Failed_Perm) continue;
                    return mineResult;
                }
            }
            return MineResult.Done;
        }
        return MineResult.Failed_Temp;
    }

    private MineResult mineTowards(BlockPos blockPos) {
        int n = Math.abs(blockPos.getX() - this.worldPosition.getX());
        int n2 = this.worldPosition.getX() < blockPos.getX() ? 1 : -1;
        int n3 = -Math.abs(blockPos.getZ() - this.worldPosition.getZ());
        int n4 = this.worldPosition.getZ() < blockPos.getZ() ? 1 : -1;
        int n5 = n + n3;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n6 = this.worldPosition.getX();
        int n7 = this.worldPosition.getZ();
        while (n6 != blockPos.getX() || n7 != blockPos.getZ()) {
            boolean bl = n6 == this.lastX && n7 == this.lastZ;
            int n8 = 2 * n5;
            if (n8 > n3) {
                n5 += n3;
                n6 += n2;
            } else if (n8 < n) {
                n5 += n;
                n7 += n4;
            }
            mutableBlockPos.set(n6, blockPos.getY(), n7);
            Level level = this.getLevel();
            BlockState blockState = level.getBlockState((BlockPos)mutableBlockPos);
            boolean bl2 = false;
            if (bl) {
                bl2 = true;
            } else if (!blockState.isAir() && (LiquidUtil.getLiquid(level, (BlockPos)mutableBlockPos) == null || LiquidUtil.getLiquid(level, (BlockPos)mutableBlockPos).isSource || this.pumpMode && this.canPump((BlockPos)mutableBlockPos))) {
                bl2 = true;
            }
            if (!bl2) continue;
            MineResult mineResult = this.mineBlock((BlockPos)mutableBlockPos, blockState);
            if (mineResult == MineResult.Done) {
                this.lastX = n6;
                this.lastZ = n7;
            }
            return mineResult;
        }
        this.lastX = this.worldPosition.getX();
        this.lastZ = this.worldPosition.getZ();
        return MineResult.Done;
    }

    private MineResult mineBlock(BlockPos blockPos, BlockState blockState) {
        int n;
        int n2;
        LiquidUtil.LiquidData liquidData;
        Mode mode;
        Level level = this.getLevel();
        Block block = blockState.getBlock();
        boolean bl = true;
        if (!blockState.isAir()) {
            bl = false;
            liquidData = LiquidUtil.getLiquid(level, blockPos);
            if (liquidData != null) {
                if (liquidData.isSource || this.pumpMode && this.canPump(blockPos)) {
                    this.liquidPos = new BlockPos((Vec3i)blockPos);
                    this.canProvideLiquid = true;
                    return this.pumpMode || this.canMine(blockPos, blockState) ? MineResult.Failed_Temp : MineResult.Failed_Perm;
                }
            } else if (!this.canMine(blockPos, blockState)) {
                return MineResult.Failed_Perm;
            }
        }
        this.canProvideLiquid = false;
        Item item = this.drillSlot.get().getItem();
        if (bl) {
            mode = Mode.MineAir;
            n2 = 3;
            n = 20;
        } else if (item == Ic2Items.DRILL) {
            mode = Mode.MineDrill;
            n2 = 6;
            n = 200;
        } else if (item == Ic2Items.DIAMOND_DRILL) {
            mode = Mode.MineDDrill;
            n2 = 20;
            n = 50;
        } else if (item == Ic2Items.IRIDIUM_DRILL) {
            mode = Mode.MineIDrill;
            n2 = 200;
            n = 20;
        } else if (item instanceof IMiningDrill) {
            mode = Mode.MineCustomDrill;
            IMiningDrill iMiningDrill = (IMiningDrill)this.drillSlot.get().getItem();
            n2 = iMiningDrill.energyUse(this.drillSlot.get(), level, blockPos, blockState);
            n = iMiningDrill.breakTime(this.drillSlot.get(), level, blockPos, blockState);
        } else {
            throw new IllegalStateException("invalid drill: " + this.drillSlot.get());
        }
        if (this.lastMode != mode) {
            this.lastMode = mode;
            this.progress = 0;
        }
        if (this.progress < n) {
            if (this.energy.useEnergy(n2)) {
                ++this.progress;
                return MineResult.Working;
            }
        } else if (bl || this.harvestBlock(blockPos, blockState)) {
            this.progress = 0;
            return MineResult.Done;
        }
        return MineResult.Failed_Temp;
    }

    private boolean harvestBlock(BlockPos blockPos, BlockState blockState) {
        int n = 2 * (this.worldPosition.getY() - blockPos.getY());
        if (this.energy.getEnergy() < (double)n) {
            return false;
        }
        Level level = this.getLevel();
        switch (this.lastMode) {
            case MineDrill: {
                if (ElectricItem.manager.use(this.drillSlot.get(), 50.0, null)) break;
                return false;
            }
            case MineDDrill: {
                if (ElectricItem.manager.use(this.drillSlot.get(), 80.0, null)) break;
                return false;
            }
            case MineIDrill: {
                if (ElectricItem.manager.use(this.drillSlot.get(), 800.0, null)) break;
                return false;
            }
            case MineCustomDrill: {
                if (((IMiningDrill)this.drillSlot.get().getItem()).breakBlock(this.drillSlot.get(), level, blockPos, blockState)) break;
                return false;
            }
            default: {
                throw new IllegalStateException("Invalid mode " + this.lastMode + " with drill: " + this.drillSlot.get());
            }
        }
        this.energy.useEnergy(n);
        for (ItemStack itemStack : StackUtil.getDrops((BlockGetter)level, blockPos, blockState, this.lastMode == Mode.MineIDrill ? 3 : 0)) {
            this.storeDrop(itemStack);
        }
        level.removeBlock(blockPos, false);
        return true;
    }

    private void storeDrop(ItemStack itemStack) {
        if (StackUtil.putInInventory(this, Direction.WEST, itemStack, true) == 0) {
            StackUtil.dropAsEntity(this.getLevel(), this.worldPosition, itemStack);
        } else {
            StackUtil.putInInventory(this, Direction.WEST, itemStack, false);
        }
    }

    public boolean canPump(BlockPos blockPos) {
        return false;
    }

    public boolean canMine(BlockPos blockPos, BlockState blockState) {
        Block block = blockState.getBlock();
        if (blockState.isAir()) {
            return true;
        }
        if (block == Ic2Blocks.MINING_PIPE || block == Ic2Blocks.MINING_PIPE_TIP || block == Blocks.CHEST) {
            return false;
        }
        if ((block == Blocks.WATER || block == Blocks.LAVA || FluidHandler.getWorldFluid(blockState) != null) && this.isPumpConnected(blockPos)) {
            return true;
        }
        Level level = this.getLevel();
        if (blockState.getDestroySpeed((BlockGetter)level, blockPos) < 0.0f) {
            return false;
        }
        if (!blockState.requiresCorrectToolForDrops()) {
            return true;
        }
        if (block == Blocks.COBWEB) {
            return true;
        }
        if (!this.drillSlot.isEmpty()) {
            return this.drillSlot.get().getItem().isCorrectToolForDrops(this.drillSlot.get(), blockState);
        }
        return false;
    }

    public boolean isPumpConnected(BlockPos blockPos) {
        Level level = this.getLevel();
        for (Direction direction : Util.ALL_DIRS) {
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
            if (!(blockEntity instanceof TileEntityPump) || ((TileEntityPump)blockEntity).pump(blockPos, true, this) == null) continue;
            return true;
        }
        return false;
    }

    public boolean isAnyPumpConnected() {
        Level level = this.getLevel();
        for (Direction direction : Util.ALL_DIRS) {
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
            if (!(blockEntity instanceof TileEntityPump)) continue;
            return true;
        }
        return false;
    }

    public ContainerBase<TileEntityMiner> createServerScreenHandler(int n, Player player) {
        return new ContainerMiner(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return new ContainerMiner(n, inventory, this);
    }

    @Override
    public void onNetworkUpdate(String string) {
        if (string.equals("active")) {
            if (this.audioSource == null) {
                this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/MinerOp.ogg", true, false, IC2.audioManager.getDefaultVolume());
            }
            if (this.getActive()) {
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
            } else if (this.audioSource != null) {
                this.audioSource.stop();
            }
        }
        super.onNetworkUpdate(string);
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
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }

    static enum Mode {
        None,
        Withdraw,
        MineAir,
        MineDrill,
        MineDDrill,
        MineIDrill,
        MineCustomDrill;

    }

    static enum MineResult {
        Working,
        Done,
        Failed_Temp,
        Failed_Perm;

    }
}

