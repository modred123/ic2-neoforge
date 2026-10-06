/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.util.StringRepresentable
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package ic2.core.block.misc;

import ic2.core.block.misc.WallBlock;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FoamBlock
extends Block {
    public static final EnumProperty<FoamType> typeProperty = EnumProperty.create((String)"type", FoamType.class);

    public FoamBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(typeProperty, FoamType.normal));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{typeProperty});
    }

    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int n = serverLevel.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (n <= 0) {
            throw new IllegalStateException("Foam was randomly ticked when world " + serverLevel + " is not ticking?");
        }
        FoamType foamType = (FoamType)((Object)blockState.getValue(typeProperty));
        float f = FoamBlock.getHardenChance((Level)serverLevel, blockPos, blockState, foamType) * 4096.0f / (float)n;
        if (randomSource.nextFloat() < f) {
            serverLevel.setBlockAndUpdate(blockPos, ((FoamType)((Object)blockState.getValue(typeProperty))).getResult());
        }
    }

    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (StackUtil.consume(player, InteractionHand.MAIN_HAND, StackUtil.sameItem((ItemLike)Blocks.SAND), 1)) {
            level.setBlockAndUpdate(blockPos, ((FoamType)((Object)blockState.getValue(typeProperty))).getResult());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static float getHardenChance(Level level, BlockPos blockPos, BlockState blockState, FoamType foamType) {
        int n = level.getMaxLocalRawBrightness(blockPos);
        if (blockState.getLightBlock((BlockGetter)level, blockPos) == 0) {
            for (Direction direction : Util.ALL_DIRS) {
                n = Math.max(n, level.getMaxLocalRawBrightness(blockPos.relative(direction)));
            }
        }
        int n2 = foamType.hardenTime * (16 - n);
        return 1.0f / (float)(n2 * 20);
    }

    public static enum FoamType implements StringRepresentable
    {
        normal(300),
        reinforced(600);

        public final int hardenTime;

        private FoamType(int n2) {
            this.hardenTime = n2;
        }

        public String getSerializedName() {
            return this.name();
        }

        public List<ItemStack> getDrops() {
            switch (this) {
                case normal: {
                    return new ArrayList<ItemStack>();
                }
                case reinforced: {
                    ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
                    return arrayList;
                }
            }
            throw new UnsupportedOperationException();
        }

        public BlockState getResult() {
            switch (this) {
                case normal: {
                    return WallBlock.get(WallBlock.DEFAULT_COLOR).defaultBlockState();
                }
                case reinforced: {
                    return Ic2Blocks.REINFORCED_STONE.defaultBlockState();
                }
            }
            throw new UnsupportedOperationException();
        }
    }
}

