/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.LiquidBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package ic2.core.util;

import ic2.core.fluid.FluidHandler;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class PumpUtil {
    private static int moveUp(Level level, BlockPos.MutableBlockPos mutableBlockPos) {
        mutableBlockPos.set(mutableBlockPos.getX(), mutableBlockPos.getY() + 1, mutableBlockPos.getZ());
        int n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n >= 0) {
            return n;
        }
        mutableBlockPos.set(mutableBlockPos.getX() + 1, mutableBlockPos.getY(), mutableBlockPos.getZ());
        n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n >= 0) {
            return n;
        }
        mutableBlockPos.set(mutableBlockPos.getX() - 2, mutableBlockPos.getY(), mutableBlockPos.getZ());
        n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n >= 0) {
            return n;
        }
        mutableBlockPos.set(mutableBlockPos.getX() + 1, mutableBlockPos.getY(), mutableBlockPos.getZ() + 1);
        n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n >= 0) {
            return n;
        }
        mutableBlockPos.set(mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ() - 2);
        n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n >= 0) {
            return n;
        }
        mutableBlockPos.set(mutableBlockPos.getX(), mutableBlockPos.getY() - 1, mutableBlockPos.getZ() + 1);
        return -1;
    }

    private static int moveSideways(Level level, BlockPos.MutableBlockPos mutableBlockPos, int n) {
        mutableBlockPos.set(mutableBlockPos.getX() - 1, mutableBlockPos.getY(), mutableBlockPos.getZ());
        int n2 = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n2 >= 0 && n2 < n) {
            return n2;
        }
        mutableBlockPos.set(mutableBlockPos.getX() + 1, mutableBlockPos.getY(), mutableBlockPos.getZ() + 1);
        n2 = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n2 >= 0 && n2 < n) {
            return n2;
        }
        mutableBlockPos.set(mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ() - 2);
        n2 = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n2 >= 0 && n2 < n) {
            return n2;
        }
        mutableBlockPos.set(mutableBlockPos.getX() + 1, mutableBlockPos.getY(), mutableBlockPos.getZ() + 1);
        n2 = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        if (n2 >= 0 && n2 < n) {
            return n2;
        }
        mutableBlockPos.set(mutableBlockPos.getX() - 1, mutableBlockPos.getY(), mutableBlockPos.getZ());
        return -1;
    }

    public static BlockPos searchFluidSource(Level level, BlockPos blockPos) {
        int n;
        int n2;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        mutableBlockPos.set(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        int n3 = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos);
        for (int i = 0; i < 64 && ((n2 = PumpUtil.moveUp(level, mutableBlockPos)) >= 0 || (n2 = PumpUtil.moveSideways(level, mutableBlockPos, n3)) >= 0); ++i) {
            n3 = n2;
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>(64);
        for (n2 = 0; n2 < 64; ++n2) {
            hashSet.add(new BlockPos((Vec3i)mutableBlockPos));
            mutableBlockPos.set(mutableBlockPos.getX() - 1, mutableBlockPos.getY(), mutableBlockPos.getZ());
            if (!hashSet.contains(mutableBlockPos) && (n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos)) >= 0) {
                if (n != 0) continue;
                return mutableBlockPos;
            }
            mutableBlockPos.set(mutableBlockPos.getX() + 1, mutableBlockPos.getY(), mutableBlockPos.getZ() + 1);
            if (!hashSet.contains(mutableBlockPos) && (n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos)) >= 0) {
                if (n != 0) continue;
                return mutableBlockPos;
            }
            mutableBlockPos.set(mutableBlockPos.getX(), mutableBlockPos.getY(), mutableBlockPos.getZ() - 2);
            if (!hashSet.contains(mutableBlockPos) && (n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos)) >= 0) {
                if (n != 0) continue;
                return mutableBlockPos;
            }
            mutableBlockPos.set(mutableBlockPos.getX() + 1, mutableBlockPos.getY(), mutableBlockPos.getZ() + 1);
            if (!hashSet.contains(mutableBlockPos) && (n = PumpUtil.getFlowDecay(level, (BlockPos)mutableBlockPos)) >= 0) {
                if (n != 0) continue;
                return mutableBlockPos;
            }
            mutableBlockPos.set(mutableBlockPos.getX() - 1, mutableBlockPos.getY(), mutableBlockPos.getZ());
            break;
        }
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (n = -2; n <= 2; ++n) {
            for (int i = -2; i <= 2; ++i) {
                mutableBlockPos2.set(mutableBlockPos.getX() + n, mutableBlockPos.getY(), mutableBlockPos.getZ() + i);
                BlockState blockState = level.getBlockState((BlockPos)mutableBlockPos2);
                n3 = PumpUtil.getFlowDecay(blockState, level, (BlockPos)mutableBlockPos2);
                if (n3 < 0) continue;
                if (n3 == 0) {
                    return mutableBlockPos2;
                }
                if (n3 < 7 && blockState.getBlock() instanceof LiquidBlock) continue;
                level.removeBlock((BlockPos)mutableBlockPos2, false);
            }
        }
        return null;
    }

    protected static int getFlowDecay(Level level, BlockPos blockPos) {
        BlockState blockState = level.getBlockState(blockPos);
        return PumpUtil.getFlowDecay(blockState, level, blockPos);
    }

    protected static int getFlowDecay(BlockState blockState, Level level, BlockPos blockPos) {
        Block block = blockState.getBlock();
        int n = FluidHandler.getWorldFluidLevel(blockState, level, blockPos);
        if (n >= 0) {
            return n;
        }
        if (block instanceof LiquidBlock) {
            return (Integer)blockState.getValue((Property)LiquidBlock.LEVEL);
        }
        return -1;
    }

    protected static boolean isExistInArray(int n, int n2, int n3, int[][] nArray, int n4) {
        for (int i = 0; i <= n4; ++i) {
            if (nArray[i][0] != n || nArray[i][1] != n2 || nArray[i][2] != n3) continue;
            return true;
        }
        return false;
    }
}

