/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerChunkCache
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.ChunkSource
 *  net.minecraft.world.level.chunk.ChunkStatus
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.chunk.PalettedContainer
 *  net.minecraft.world.level.lighting.LevelLightEngine
 */
package ic2.core.util;

import ic2.core.IC2;
import ic2.core.proxy.EnvProxy;
import ic2.core.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public final class BiomeUtil {
    private static final Field field_ChunkSection_biomeContainer = ReflectionUtil.getField(LevelChunkSection.class, "biomeContainer", "field_34556", "biomes");

    public static Biome getBiome(Level level, ResourceKey<Biome> resourceKey) {
        return (Biome)level.registryAccess().registryOrThrow(Registries.BIOME).get(resourceKey);
    }

    public static Holder<Biome> getOriginalBiome(LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getUncachedNoiseBiome(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static Holder<Biome> getBiome(LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBiome(blockPos);
    }

    public static void setBiome(LevelReader levelReader, BlockPos blockPos, Biome biome) {
        Objects.requireNonNull(biome, "null biome");
        int n = blockPos.getX() >> 4;
        int n2 = blockPos.getZ() >> 4;
        ChunkAccess chunkAccess = levelReader.getChunk(n, n2, ChunkStatus.BIOMES);
        for (LevelChunkSection levelChunkSection : chunkAccess.getSections()) {
            PalettedContainer palettedContainer = (PalettedContainer)ReflectionUtil.getFieldValue(field_ChunkSection_biomeContainer, levelChunkSection);
            for (int i = 0; i < 3; ++i) {
                palettedContainer.set(n >> 2, i, n2 >> 2, (Object)biome);
            }
        }
    }

    public static void setBiomeAndNotify(Level level, BlockPos blockPos, Biome biome) {
        BiomeUtil.setBiome((LevelReader)level, blockPos, biome);
        ChunkSource chunkSource = level.getChunkSource();
        if (chunkSource instanceof ServerChunkCache) {
            LevelChunk levelChunk = level.getChunkAt(blockPos);
            ClientboundLevelChunkWithLightPacket clientboundLevelChunkWithLightPacket = new ClientboundLevelChunkWithLightPacket(levelChunk, (LevelLightEngine)((ServerLevel)level).getChunkSource().getLightEngine(), null, null);
            ((ServerChunkCache)chunkSource).chunkMap.getPlayers(levelChunk.getPos(), false).forEach(player -> player.connection.send(clientboundLevelChunkWithLightPacket));
            levelChunk.setUnsaved(true);
        } else {
            assert (!level.isClientSide) : "Can't notify a server of a client side biome change";
            level.getChunkAt(blockPos).setUnsaved(true);
        }
    }

    public static int getBiomeTemperature(Level level, BlockPos blockPos) {
        Holder<Biome> holder = BiomeUtil.getBiome((LevelReader)level, blockPos);
        if (IC2.envProxy.biomeHasType(holder, EnvProxy.BiomeType.HOT)) {
            return 45;
        }
        if (IC2.envProxy.biomeHasType(holder, EnvProxy.BiomeType.COLD)) {
            return 0;
        }
        return 25;
    }
}

