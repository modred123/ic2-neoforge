/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Registry
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.chunk.PalettedContainer
 */
package ic2.core.block;

import ic2.core.block.ChunkLoadAwareBlock;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public final class ChunkLoadAwareBlockHandler {
    private static final int CS = 16;
    private static final int CSM = 15;
    private static final Map<BlockState, ChunkLoadAwareBlock> stateMap = new IdentityHashMap<BlockState, ChunkLoadAwareBlock>();

    public static void init() {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof ChunkLoadAwareBlock)) continue;
            ChunkLoadAwareBlock chunkLoadAwareBlock = (ChunkLoadAwareBlock)block;
            for (BlockState blockState : chunkLoadAwareBlock.getLoadAwareState(block)) {
                stateMap.put(blockState, chunkLoadAwareBlock);
            }
        }
        // 1.21 迁移诊断：确认 onInitLate 的 requestTick(true) 调度链真的执行（进入世界后
        // 电缆区块加载依赖此 stateMap）。
        org.apache.logging.log4j.LogManager.getLogger("ic2-diag").info(
            "ChunkLoadAwareBlockHandler.init done: {} states mapped, thread={}",
            stateMap.size(), Thread.currentThread().getName());
    }

    public static void onChunkLoad(LevelChunk levelChunk) {
        ChunkLoadAwareBlockHandler.processChunk(levelChunk, true);
    }

    public static void onChunkUnload(LevelChunk levelChunk) {
        ChunkLoadAwareBlockHandler.processChunk(levelChunk, false);
    }

    private static void processChunk(LevelChunk levelChunk, boolean bl) {
        LevelChunkSection[] sections = levelChunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; ++sectionIndex) {
            LevelChunkSection levelChunkSection = sections[sectionIndex];
            if (levelChunkSection.hasOnlyAir()) continue;
            PalettedContainer palettedContainer = levelChunkSection.getStates();
            Map<BlockState, ChunkLoadAwareBlock> map = stateMap;
            Objects.requireNonNull(map);
            if (!palettedContainer.maybeHas((Predicate<BlockState>)map::containsKey)) continue;
            Level level = levelChunk.getLevel();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(levelChunk.getPos().getMinBlockX(), levelChunk.getMinBuildHeight() + sectionIndex * 16, levelChunk.getPos().getMinBlockZ());
            BlockState blockState = null;
            ChunkLoadAwareBlock chunkLoadAwareBlock = null;
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    for (int k = 0; k < 16; ++k) {
                        BlockState blockState2 = (BlockState)palettedContainer.get(k, i, j);
                        if (blockState2 != blockState) {
                            blockState = blockState2;
                            chunkLoadAwareBlock = stateMap.get(blockState2);
                        }
                        if (chunkLoadAwareBlock == null) continue;
                        mutableBlockPos.set(mutableBlockPos.getX() & 0xFFFFFFF0 | k, mutableBlockPos.getY() & 0xFFFFFFF0 | i, mutableBlockPos.getZ() & 0xFFFFFFF0 | j);
                        if (bl) {
                            chunkLoadAwareBlock.onLoad(blockState2, level, (BlockPos)mutableBlockPos);
                            continue;
                        }
                        chunkLoadAwareBlock.onUnload(blockState2, level, (BlockPos)mutableBlockPos);
                    }
                }
            }
        }
    }
}

