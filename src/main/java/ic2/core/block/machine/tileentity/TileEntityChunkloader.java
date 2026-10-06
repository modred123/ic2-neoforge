/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ChunkLoaderLogic;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChunkloader
extends TileEntityInventory
implements INetworkClientTileEntityEventListener,
IHasGui,
IUpgradableBlock {
    private final LongSet loadedChunks = new LongOpenHashSet();
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotDischarge dischargeSlot;
    public final Energy energy;
    private static final int defaultTier = 1;
    private static final int defaultEnergyStorage = 2500;
    private static final int range = 4;
    private final double euPerChunk = ConfigUtil.getFloat(MainConfig.get(), "balance/euPerChunk");

    public TileEntityChunkloader(BlockPos blockPos, BlockState blockState) {
        super((BlockEntityType<? extends TileEntityInventory>)Ic2BlockEntities.CHUNK_LOADER, blockPos, blockState);
        this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.IO, 1, true, InvSlot.InvSide.ANY);
        this.energy = this.addComponent(Energy.asBasicSink(this, 2500.0, 1).addManagedSlot(this.dischargeSlot));
    }

    @Override
    public void updateEntityServer() {
        super.updateEntityServer();
        boolean bl = this.energy.useEnergy((double)this.getLoadedChunks().size() * this.euPerChunk);
        if (bl != this.getActive()) {
            this.setActive(bl);
        }
        this.upgradeSlot.tick();
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        ListTag listTag = compoundTag.getList("loadedChunks", 4);
        this.loadedChunks.clear();
        for (int i = 0; i < listTag.size(); ++i) {
            this.loadedChunks.add(((LongTag)listTag.get(i)).getAsLong());
        }
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        ListTag listTag = new ListTag();
        compoundTag.put("loadedChunks", (Tag)listTag);
        LongIterator longIterator = this.loadedChunks.iterator();
        while (longIterator.hasNext()) {
            long l = (Long)longIterator.next();
            listTag.add(LongTag.valueOf(l));
        }
    }

    @Override
    public void setActive(boolean bl) {
        Level level = this.getLevel();
        if (!level.isClientSide && this.getActive() != bl) {
            if (bl) {
                ChunkLoaderLogic.addChunkLoader((ServerLevel)level, this.worldPosition, this.loadedChunks);
            } else {
                ChunkLoaderLogic.removeChunkLoader((ServerLevel)level, this.worldPosition);
            }
        }
        super.setActive(bl);
    }

    @Override
    public void onLoaded() {
        super.onLoaded();
        Level level = this.getLevel();
        if (!level.isClientSide) {
            this.setOverclockRates();
            if (this.getActive()) {
                ChunkLoaderLogic.addChunkLoader((ServerLevel)level, this.worldPosition, this.loadedChunks);
            }
        }
    }

    @Override
    public void onPlaced(ItemStack itemStack, LivingEntity livingEntity, Direction direction) {
        super.onPlaced(itemStack, livingEntity, direction);
        this.loadedChunks.add(ChunkPos.asLong((BlockPos)this.worldPosition));
    }

    @Override
    public ContainerBase<?> createServerScreenHandler(int n, Player player) {
        return new ContainerChunkLoader(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return new ContainerChunkLoader(n, inventory, this);
    }

    public void addChunkToLoaded(ChunkPos chunkPos) {
        if (this.getLevel().isClientSide) {
            new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
            return;
        }
        if (!this.isChunkInRange(chunkPos)) {
            IC2.log.warn(LogCategory.Block, "Trying to add a Chunk to loaded, however the chunk is too far away. Aborting.");
            return;
        }
        if (this.loadedChunks.add(chunkPos.toLong())) {
            ChunkLoaderLogic.updateChunkLoader((ServerLevel)this.getLevel(), this.worldPosition, this.loadedChunks);
            this.setChanged();
        }
    }

    public void removeChunkFromLoaded(ChunkPos chunkPos) {
        if (this.getLevel().isClientSide) {
            new RuntimeException("Something tried to change the ChunkLoaderState on the client.").printStackTrace();
            return;
        }
        if (ChunkPos.asLong((BlockPos)this.worldPosition) == chunkPos.toLong()) {
            return;
        }
        if (this.loadedChunks.remove(chunkPos.toLong())) {
            ChunkLoaderLogic.updateChunkLoader((ServerLevel)this.getLevel(), this.worldPosition, this.loadedChunks);
            this.setChanged();
        }
    }

    public LongSet getLoadedChunks() {
        return this.loadedChunks;
    }

    public boolean isChunkInRange(ChunkPos chunkPos) {
        ChunkPos chunkPos2 = new ChunkPos(this.worldPosition);
        return Math.abs(chunkPos.x - chunkPos2.x) <= 4 && Math.abs(chunkPos.z - chunkPos2.z) <= 4;
    }

    public int getMaxChunks() {
        return 9;
    }

    @Override
    public void onNetworkEvent(Player player, int n) {
        int n2 = (n & 0xF) - 8;
        int n3 = (n >> 4 & 0xF) - 8;
        ChunkPos chunkPos = new ChunkPos(this.worldPosition);
        ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + n2, chunkPos.z + n3);
        if (this.isChunkInRange(chunkPos2)) {
            if (this.getLoadedChunks().contains(chunkPos2.toLong())) {
                this.removeChunkFromLoaded(chunkPos2);
            } else {
                this.addChunkToLoaded(chunkPos2);
            }
        }
    }

    @Override
    protected void onBlockBreak() {
        super.onBlockBreak();
        ChunkLoaderLogic.removeChunkLoader((ServerLevel)this.getLevel(), this.worldPosition);
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
        return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.Transformer);
    }

    public void setOverclockRates() {
        this.upgradeSlot.onChanged();
        int n = this.upgradeSlot.getTier(1);
        this.energy.setSinkTier(n);
        this.dischargeSlot.setTier(n);
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(2500, 0, 0));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (IC2.sideProxy.isSimulating()) {
            this.setOverclockRates();
        }
    }
}

