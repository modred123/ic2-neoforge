package ic2.core;

import ic2.core.IWorldTickCallback;
import ic2.core.WindSim;
import ic2.core.block.personal.TradingMarket;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.network.TeUpdateDataServer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class WorldData {
    private static ConcurrentMap<ResourceLocation, WorldData> idxClient = IC2.envProxy.isClientEnv() ? new ConcurrentHashMap() : null;
    private static ConcurrentMap<ResourceLocation, WorldData> idxServer = new ConcurrentHashMap<ResourceLocation, WorldData>();
    final Queue<IWorldTickCallback> singleUpdates = new ConcurrentLinkedQueue<IWorldTickCallback>();
    final Set<IWorldTickCallback> continuousUpdates = new HashSet<IWorldTickCallback>();
    boolean continuousUpdatesInUse = false;
    final List<IWorldTickCallback> continuousUpdatesToAdd = new ArrayList<IWorldTickCallback>();
    final List<IWorldTickCallback> continuousUpdatesToRemove = new ArrayList<IWorldTickCallback>();
    public final EnergyNetLocal energyNet;
    public final Map<BlockEntity, TeUpdateDataServer> tesToUpdate = new IdentityHashMap<BlockEntity, TeUpdateDataServer>();
    public final TradingMarket tradeMarket;
    public final WindSim windSim;
    public final Map<LevelChunk, CompoundTag> worldGenData = new IdentityHashMap<LevelChunk, CompoundTag>();
    public final Set<LevelChunk> chunksToDecorate = Collections.newSetFromMap(new IdentityHashMap());
    public final Set<LevelChunk> pendingUnloadChunks = Collections.newSetFromMap(new IdentityHashMap());

    private WorldData(Level world) {
        if (!world.isClientSide) {
            this.energyNet = EnergyNetLocal.create(world);
            this.tradeMarket = new TradingMarket(world);
            this.windSim = new WindSim(world);
        } else {
            this.energyNet = null;
            this.tradeMarket = null;
            this.windSim = null;
        }
    }

    public static WorldData get(Level world) {
        return WorldData.get(world, true);
    }

    public static WorldData get(Level world, boolean load) {
        if (world == null) {
            throw new IllegalArgumentException("world is null");
        }
        ConcurrentMap<ResourceLocation, WorldData> index = WorldData.getIndex(!world.isClientSide);
        ResourceLocation key = WorldData.getKey(world);
        WorldData ret = (WorldData)index.get(key);
        if (ret != null || !load) {
            return ret;
        }
        ret = new WorldData(world);
        WorldData prev = index.putIfAbsent(key, ret);
        if (prev != null) {
            ret = prev;
        }
        return ret;
    }

    public static void onWorldUnload(Level world) {
        WorldData.getIndex(!world.isClientSide).remove(WorldData.getKey(world));
    }

    private static ResourceLocation getKey(Level world) {
        return world.dimension().location();
    }

    private static ConcurrentMap<ResourceLocation, WorldData> getIndex(boolean simulating) {
        return simulating ? idxServer : idxClient;
    }
}
