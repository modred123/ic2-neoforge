/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.RandomSource
 *  net.minecraft.util.valueproviders.ConstantInt
 *  net.minecraft.util.valueproviders.IntProvider
 *  net.minecraft.world.level.LevelSimulatedReader
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
 *  net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
 *  net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer$FoliageAttachment
 *  net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
 */
package ic2.core.world;

import com.mojang.serialization.Codec;
import ic2.core.IC2;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public final class RubberTreeFoliagePlacer
extends FoliagePlacer {
    public static final RubberTreeFoliagePlacer INSTANCE = new RubberTreeFoliagePlacer();
    public static final Codec<RubberTreeFoliagePlacer> CODEC = Codec.unit(INSTANCE);
    public static final FoliagePlacerType<?> TYPE = RubberTreeFoliagePlacer.registerFoliagePlacer("rubber_tree", CODEC);

    public static void init() {
    }

    RubberTreeFoliagePlacer() {
        super((IntProvider)ConstantInt.of((int)2), (IntProvider)ConstantInt.of((int)0));
    }

    protected FoliagePlacerType<?> type() {
        return TYPE;
    }

    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int n, FoliagePlacer.FoliageAttachment foliageAttachment, int n2, int n3, int n4) {
        int n5;
        int n6;
        int n7 = n < 4 ? 0 : (n < 7 ? 2 : 3);
        BlockPos blockPos = foliageAttachment.pos();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (n6 = n7; n6 < n; ++n6) {
            for (n5 = -n3; n5 <= n3; ++n5) {
                for (int i = -n3; i <= n3; ++i) {
                    mutableBlockPos.setWithOffset((Vec3i)blockPos, n5, n6 - n, i);
                    int n8 = n6 + 4 - n;
                    int n9 = Math.abs(n5);
                    int n10 = Math.abs(i);
                    if ((n9 > 1 || n10 > 1) && (n9 > 1 || n8 > 1 && randomSource.nextInt(n8) != 0) && (n10 > 1 || n8 > 1 && randomSource.nextInt(n8) != 0)) continue;
                    FoliagePlacer.tryPlaceLeaf((LevelSimulatedReader)levelSimulatedReader, foliageSetter, (RandomSource)randomSource, (TreeConfiguration)treeConfiguration, (BlockPos)mutableBlockPos);
                }
            }
        }
        for (n6 = -1; n6 <= 1; ++n6) {
            for (n5 = -1; n5 <= 1; ++n5) {
                if (n6 != 0 && n5 != 0) continue;
                mutableBlockPos.setWithOffset((Vec3i)blockPos, n6, 0, n5);
                FoliagePlacer.tryPlaceLeaf((LevelSimulatedReader)levelSimulatedReader, foliageSetter, (RandomSource)randomSource, (TreeConfiguration)treeConfiguration, (BlockPos)mutableBlockPos);
            }
        }
        for (n6 = 0; n6 < n2; ++n6) {
            mutableBlockPos.setWithOffset((Vec3i)blockPos, 0, n6 + 1, 0);
            FoliagePlacer.tryPlaceLeaf((LevelSimulatedReader)levelSimulatedReader, foliageSetter, (RandomSource)randomSource, (TreeConfiguration)treeConfiguration, (BlockPos)mutableBlockPos);
        }
    }

    public int foliageHeight(RandomSource randomSource, int n, TreeConfiguration treeConfiguration) {
        return 1 + n / 4 + randomSource.nextInt(2);
    }

    protected boolean shouldSkipLocation(RandomSource randomSource, int n, int n2, int n3, int n4, boolean bl) {
        return false;
    }

    private static <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(String string, Codec<T> codec) {
        return IC2.envProxy.registerFoliagePlacer(IC2.getIdentifier(string), codec);
    }
}

