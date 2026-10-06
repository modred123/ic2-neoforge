/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.SupportType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package ic2.core.block.misc;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldBlock
extends Block {
    private static final IRecipeInput stickInput = Recipes.inputFactory.forItem((ItemLike)Items.STICK);
    private static final Direction[] supportedFacings = new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final double border = 0.03125;
    private static final VoxelShape aabb = Shapes.box((double)0.03125, (double)0.0, (double)0.03125, (double)0.96875, (double)1.0, (double)0.96875);
    private final int maxDistance;

    public ScaffoldBlock(BlockBehaviour.Properties properties, int n) {
        super(properties);
        this.maxDistance = n;
    }

    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.fallDistance = 0.0f;
            double d = 0.15;
            Vec3 vec3 = livingEntity.getDeltaMovement();
            double d2 = Util.limit(vec3.x(), -d, d);
            double d3 = Util.limit(vec3.z(), -d, d);
            livingEntity.setDeltaMovement(d2, vec3.y(), d3);
            if (livingEntity.isShiftKeyDown() && livingEntity instanceof Player) {
                if (livingEntity.isInWater()) {
                    livingEntity.setDeltaMovement(d2, 0.02, d3);
                } else {
                    livingEntity.setDeltaMovement(d2, 0.08, d3);
                }
            } else if (livingEntity.horizontalCollision) {
                livingEntity.setDeltaMovement(d2, 0.2, d3);
            } else {
                livingEntity.setDeltaMovement(d2, -0.07, d3);
            }
        }
    }

    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return aabb;
    }

    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.block();
    }

    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.block();
    }

    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        Block block;
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (StackUtil.isEmpty(itemStack)) {
            return InteractionResult.PASS;
        }
        Block block2 = blockState.getBlock();
        if (block2 == Ic2Blocks.WOODEN_SCAFFOLD) {
            if (!stickInput.matches(itemStack) || StackUtil.getSize(itemStack) < 2) {
                return InteractionResult.PASS;
            }
            block = Ic2Blocks.REINFORCED_WOODEN_SCAFFOLD;
        } else if (block2 == Ic2Blocks.IRON_SCAFFOLD) {
            if (!StackUtil.checkItemEquality(itemStack, new ItemStack((ItemLike)Ic2Blocks.IRON_FENCE)) || StackUtil.getSize(itemStack) < 1) {
                return InteractionResult.PASS;
            }
            block = Ic2Blocks.REINFORCED_IRON_SCAFFOLD;
        } else {
            return InteractionResult.PASS;
        }
        if (!this.isPillar(level, blockPos)) {
            return InteractionResult.PASS;
        }
        if (block2 == Ic2Blocks.WOODEN_SCAFFOLD) {
            StackUtil.consumeOrError(player, InteractionHand.MAIN_HAND, StackUtil.recipeInput(stickInput), 2);
        } else {
            StackUtil.consumeOrError(player, InteractionHand.MAIN_HAND, StackUtil.sameStack(new ItemStack((ItemLike)Ic2Blocks.IRON_FENCE)), 1);
        }
        level.setBlockAndUpdate(blockPos, block.defaultBlockState());
        return InteractionResult.SUCCESS;
    }

    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (StackUtil.isEmpty(itemStack)) {
            return;
        }
        if (StackUtil.checkItemEquality(itemStack, Item.byBlock((Block)this))) {
            while (level.getBlockState(blockPos).getBlock() == this) {
                blockPos = blockPos.above();
            }
            if (this.canSurvive(this.defaultBlockState(), (LevelReader)level, blockPos) && blockPos.getY() < IC2.getWorldMaxHeight(level)) {
                boolean bl = player.getAbilities().instabuild;
                ItemStack itemStack2 = bl ? StackUtil.copy(itemStack) : null;
                itemStack.useOn((UseOnContext)new BlockPlaceContext(player, InteractionHand.MAIN_HAND, itemStack, new BlockHitResult(new Vec3(0.5, 1.0, 0.5).add((double)blockPos.below().getX(), (double)blockPos.below().getY(), (double)blockPos.below().getZ()), Direction.UP, blockPos.below(), true)));
                if (!bl) {
                    StackUtil.clearEmpty(player, InteractionHand.MAIN_HAND);
                } else {
                    StackUtil.set(player, InteractionHand.MAIN_HAND, itemStack2);
                }
            }
        }
    }

    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return super.canSurvive(blockState, levelReader, blockPos) && this.hasSupport((BlockGetter)levelReader, blockPos, this);
    }

    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        this.checkSupport(level, blockPos);
    }

    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(8) == 0) {
            this.checkSupport((Level)serverLevel, blockPos);
        }
    }

    private boolean isPillar(Level level, BlockPos blockPos) {
        while (level.getBlockState(blockPos).getBlock() == this) {
            blockPos = blockPos.below();
        }
        return level.getBlockState(blockPos).isFaceSturdy((BlockGetter)level, blockPos, Direction.UP, SupportType.FULL);
    }

    private boolean hasSupport(BlockGetter blockGetter, BlockPos blockPos, ScaffoldBlock scaffoldBlock) {
        return this.calculateSupport((BlockGetter)blockGetter, (BlockPos)blockPos, (ScaffoldBlock)scaffoldBlock).get((Object)blockPos).strength >= 0;
    }

    private void checkSupport(Level level, BlockPos blockPos) {
        Block block = level.getBlockState(blockPos).getBlock();
        if (!(block instanceof ScaffoldBlock)) {
            return;
        }
        Map<BlockPos, Support> map = this.calculateSupport((BlockGetter)level, blockPos, (ScaffoldBlock)block);
        boolean bl = false;
        for (Support support : map.values()) {
            if (support.strength >= 0) continue;
            level.setBlock(support.pos, Blocks.AIR.defaultBlockState(), 2);
            Block.dropResources((BlockState)support.block.defaultBlockState(), (Level)level, (BlockPos)support.pos);
            bl = true;
        }
        if (bl) {
            for (Support support : map.values()) {
                if (support.strength >= 0) continue;
                level.blockUpdated(support.pos, (Block)this);
            }
        }
    }

    private Map<BlockPos, Support> calculateSupport(BlockGetter blockGetter, BlockPos blockPos, ScaffoldBlock scaffoldBlock) {
        HashMap<BlockPos, Support> hashMap = new HashMap<BlockPos, Support>();
        ArrayDeque<Support> arrayDeque = new ArrayDeque<Support>();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        Support support = new Support(blockPos, scaffoldBlock, -1);
        hashMap.put(blockPos, support);
        arrayDeque.add(support);
        while ((support = (Support)arrayDeque.poll()) != null) {
            for (Direction direction : Util.ALL_DIRS) {
                BlockPos blockPos2 = support.pos.relative(direction);
                if (hashMap.containsKey(blockPos2)) continue;
                BlockState object = blockGetter.getBlockState(blockPos2);
                Block block = object.getBlock();
                if (block instanceof ScaffoldBlock) {
                    Support support2 = new Support(blockPos2, (ScaffoldBlock)block, -1);
                    hashMap.put(blockPos2, support2);
                    arrayDeque.add(support2);
                    continue;
                }
                if (!object.isCollisionShapeFullBlock(blockGetter, blockPos2)) continue;
                hashSet.add(blockPos2);
            }
        }
        for (BlockPos blockPos3 : hashSet) {
            BlockPos blockPos4 = blockPos3.above();
            int n = 0;
            while ((support = (Support)hashMap.get(blockPos4)) != null) {
                int n2;
                if (support.block.maxDistance >= n) {
                    n2 = support.block.maxDistance;
                    n = n2 - 1;
                } else {
                    n2 = n--;
                }
                if (support.strength < n2) {
                    support.strength = n2;
                    for (Direction direction : Util.HORIZONTAL_DIRS) {
                        BlockPos blockPos2 = blockPos4.relative(direction);
                        Support support3 = (Support)hashMap.get(blockPos2);
                        if (support3 == null || support3.strength >= n2) continue;
                        support3.strength = n2 - 1;
                        arrayDeque.add(support3);
                    }
                }
                blockPos4 = blockPos4.above();
            }
        }
        while ((support = (Support)arrayDeque.poll()) != null) {
            for (Direction direction : supportedFacings) {
                BlockPos blockPos6 = support.pos.relative(direction);
                Support support4 = (Support)hashMap.get(blockPos6);
                if (support4 == null || support4.strength >= support.strength) continue;
                support4.strength = support.strength - 1;
                if (support4.strength <= 0) continue;
                arrayDeque.add(support4);
            }
        }
        return hashMap;
    }

    private static class Support {
        final BlockPos pos;
        final ScaffoldBlock block;
        int strength;

        Support(BlockPos blockPos, ScaffoldBlock scaffoldBlock, int n) {
            this.pos = blockPos;
            this.block = scaffoldBlock;
            this.strength = n;
        }
    }
}

