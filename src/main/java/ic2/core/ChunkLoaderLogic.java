/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectFunction
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.Object2ObjectFunction
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.TicketType
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.saveddata.SavedData
 */
package ic2.core;

import ic2.core.IC2;
import ic2.core.event.WorldData;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;

public final class ChunkLoaderLogic {
    private static final String savedStateId = IC2.getIdentifier("loaded_chunks").toString().replace(':', '_');
    private static final TicketType<ChunkPos> ticketType = TicketType.create((String)IC2.getIdentifier("chunk_loader").toString(), Comparator.comparingLong((ToLongFunction<ChunkPos>)ChunkPos::toLong));

    public static void addChunkLoader(ServerLevel serverLevel, BlockPos blockPos, LongSet longSet) {
        long l = ChunkPos.asLong((BlockPos)blockPos);
        if (!longSet.contains(l)) {
            throw new IllegalArgumentException("missing own position");
        }
        SavedState savedState = (SavedState)serverLevel.getDataStorage().computeIfAbsent(SavedState.FACTORY, savedStateId);
        savedState.chunksToChunkLoaders.computeIfAbsent(l, k -> new ObjectOpenHashSet(1)).add(blockPos);
        WorldData worldData = WorldData.get((Level)serverLevel);
        LongIterator longIterator = longSet.iterator();
        while (longIterator.hasNext()) {
            long l2 = (Long)longIterator.next();
            Set<BlockPos> set = worldData.loadedChunks.computeIfAbsent(l2, k -> new ObjectOpenHashSet(1));
            if (set.isEmpty()) {
                ChunkLoaderLogic.addChunkTicket(serverLevel, new ChunkPos(l2));
            }
            set.add(blockPos);
        }
        worldData.chunkLoaders.put(blockPos, new LongOpenHashSet(longSet));
    }

    public static void removeChunkLoader(ServerLevel serverLevel, BlockPos blockPos) {
        WorldData worldData;
        long l;
        Set<BlockPos> set;
        SavedState savedState = (SavedState)serverLevel.getDataStorage().get(SavedState.FACTORY, savedStateId);
        if (savedState != null && (set = savedState.chunksToChunkLoaders.get(l = ChunkPos.asLong(blockPos))) != null && set.remove(blockPos) && set.isEmpty()) {
            savedState.chunksToChunkLoaders.remove(l);
        }
        if ((worldData = WorldData.get((Level)serverLevel, false)) != null) {
            ChunkLoaderLogic.disableChunkLoader(serverLevel, blockPos, worldData);
        }
    }

    private static void disableChunkLoader(ServerLevel serverLevel, BlockPos blockPos, WorldData worldData) {
        LongSet longSet = worldData.chunkLoaders.remove(blockPos);
        if (longSet != null) {
            LongIterator longIterator = longSet.iterator();
            while (longIterator.hasNext()) {
                long l = (Long)longIterator.next();
                Set<BlockPos> set = worldData.loadedChunks.get(l);
                if (set == null || !set.remove(blockPos) || !set.isEmpty()) continue;
                worldData.loadedChunks.remove(l);
                ChunkLoaderLogic.removeChunkTicket(serverLevel, new ChunkPos(l));
            }
        }
    }

    public static void updateChunkLoader(ServerLevel serverLevel, BlockPos blockPos, LongSet longSet) {
        Set<BlockPos> set;
        long l;
        long l2 = ChunkPos.asLong((BlockPos)blockPos);
        if (!longSet.contains(l2)) {
            throw new IllegalArgumentException("missing own position");
        }
        WorldData worldData = WorldData.get((Level)serverLevel);
        LongSet longSet2 = worldData.chunkLoaders.get(blockPos);
        if (longSet2 == null) {
            ChunkLoaderLogic.addChunkLoader(serverLevel, blockPos, longSet);
            return;
        }
        LongIterator longIterator = longSet2.longIterator();
        while (longIterator.hasNext()) {
            l = longIterator.nextLong();
            if (longSet.contains(l)) continue;
            longIterator.remove();
            set = worldData.loadedChunks.get(l);
            set.remove(blockPos);
            if (!set.isEmpty()) continue;
            ChunkLoaderLogic.removeChunkTicket(serverLevel, new ChunkPos(l));
            worldData.loadedChunks.remove(l);
        }
        longIterator = longSet.iterator();
        while (longIterator.hasNext()) {
            l = (Long)longIterator.next();
            if (!longSet2.add(l)) continue;
            set = worldData.loadedChunks.computeIfAbsent(l, k -> new ObjectOpenHashSet());
            if (set.isEmpty()) {
                ChunkLoaderLogic.addChunkTicket(serverLevel, new ChunkPos(l));
            }
            set.add(blockPos);
        }
    }

    public static void onWorldLoad(ServerLevel serverLevel) {
        SavedState savedState = (SavedState)serverLevel.getDataStorage().get(SavedState.FACTORY, savedStateId);
        if (savedState == null || savedState.chunksToChunkLoaders.isEmpty()) {
            return;
        }
        WorldData worldData = WorldData.get((Level)serverLevel);
        for (Long2ObjectMap.Entry<Set<BlockPos>> entry : savedState.chunksToChunkLoaders.long2ObjectEntrySet()) {
            long l = entry.getLongKey();
            Set<BlockPos> set = entry.getValue();
            worldData.loadedChunks.put(l, new ObjectOpenHashSet(set));
            for (BlockPos blockPos : set) {
                worldData.chunkLoaders.computeIfAbsent(blockPos, k -> new LongOpenHashSet(1)).add(l);
            }
            ChunkLoaderLogic.addChunkTicket(serverLevel, new ChunkPos(l));
        }
    }

    public static void onChunkUnload(LevelChunk levelChunk) {
        assert (!levelChunk.getLevel().isClientSide);
        ServerLevel serverLevel = (ServerLevel)levelChunk.getLevel();
        SavedState savedState = (SavedState)serverLevel.getDataStorage().get(SavedState.FACTORY, savedStateId);
        if (savedState == null || savedState.chunksToChunkLoaders.isEmpty()) {
            return;
        }
        Set<BlockPos> set = savedState.chunksToChunkLoaders.get(levelChunk.getPos().toLong());
        if (set == null || set.isEmpty()) {
            return;
        }
        WorldData worldData = WorldData.get((Level)serverLevel, false);
        if (worldData == null) {
            return;
        }
        for (BlockPos blockPos : set) {
            ChunkLoaderLogic.disableChunkLoader(serverLevel, blockPos, worldData);
        }
    }

    private static void addChunkTicket(ServerLevel serverLevel, ChunkPos chunkPos) {
        serverLevel.getChunkSource().addRegionTicket(ticketType, chunkPos, 2, chunkPos);
    }

    private static void removeChunkTicket(ServerLevel serverLevel, ChunkPos chunkPos) {
        serverLevel.getChunkSource().removeRegionTicket(ticketType, chunkPos, 2, chunkPos);
    }




    private static final class SavedState
    extends SavedData {
        final Long2ObjectMap<Set<BlockPos>> chunksToChunkLoaders = new Long2ObjectOpenHashMap();
        static final SavedData.Factory<SavedState> FACTORY = new SavedData.Factory<>(
            SavedState::new,
            (compoundTag, savedState) -> new SavedState(compoundTag),
            null
        );

        SavedState() {
        }

        SavedState(CompoundTag compoundTag) {
            ListTag listTag = compoundTag.getList("loaders", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag2 = listTag.getCompound(i);
                BlockPos blockPos = new BlockPos(compoundTag2.getInt("x"), compoundTag2.getInt("y"), compoundTag2.getInt("z"));
                this.chunksToChunkLoaders.computeIfAbsent(ChunkPos.asLong(blockPos), k -> new ObjectOpenHashSet(1)).add(blockPos);
            }
        }

        public CompoundTag save(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider provider) {
            ListTag listTag = new ListTag();
            compoundTag.put("loaders", (Tag)listTag);
            for (Set<BlockPos> set : this.chunksToChunkLoaders.values()) {
                for (BlockPos blockPos : set) {
                    CompoundTag compoundTag2 = new CompoundTag();
                    listTag.add(compoundTag2);
                    compoundTag2.putInt("x", blockPos.getX());
                    compoundTag2.putInt("y", blockPos.getY());
                    compoundTag2.putInt("z", blockPos.getZ());
                }
            }
            return compoundTag;
        }

    }
}

