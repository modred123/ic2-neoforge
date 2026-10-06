/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.EntityCollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package ic2.core.block.inherit;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Ic2SheetBlock
extends Block {
    private static final VoxelShape aabb = Shapes.box((double)0.0, (double)0.0, (double)0.0, (double)1.0, (double)0.125, (double)1.0);
    private static final Direction[] positiveHorizontalFacings = new Direction[]{Direction.EAST, Direction.SOUTH};

    public Ic2SheetBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return aabb;
    }

    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        EntityCollisionContext entityCollisionContext;
        if (blockState.getBlock() == Ic2Blocks.RESIN_SHEET) {
            return Shapes.empty();
        }
        if (blockState.getBlock() == Ic2Blocks.WOOL_SHEET && collisionContext instanceof EntityCollisionContext && (entityCollisionContext = (EntityCollisionContext)collisionContext).getEntity() instanceof Player && (entityCollisionContext.getEntity().isShiftKeyDown() || entityCollisionContext.getEntity().getY() < (double)blockPos.getY() + 0.125 - (double)entityCollisionContext.getEntity().maxUpStep())) {
            return Shapes.empty();
        }
        return aabb;
    }

    private boolean isValidPosition(Level level, BlockPos blockPos, BlockState blockState) {
        if (blockState.getBlock() == Ic2Blocks.RESIN_SHEET) {
            return this.isNormalCubeBelow(level, blockPos);
        }
        if (blockState.getBlock() == Ic2Blocks.RUBBER_SHEET) {
            for (Direction direction : Util.HORIZONTAL_DIRS) {
                blockState = level.getBlockState(blockPos.relative(direction));
                if (blockState != Ic2Blocks.RUBBER_SHEET.defaultBlockState() && !blockState.isCollisionShapeFullBlock(level, blockPos)) continue;
                return true;
            }
            return this.isNormalCubeBelow(level, blockPos);
        }
        return blockState.getBlock() == Ic2Blocks.WOOL_SHEET;
    }

    private boolean isNormalCubeBelow(Level level, BlockPos blockPos) {
        blockPos = blockPos.below();
        BlockState blockState = level.getBlockState(blockPos);
        return blockState.isCollisionShapeFullBlock(level, blockPos);
    }

    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!this.isValidPosition(level, blockPos, blockState)) {
            Block.dropResources((BlockState)blockState, (LevelAccessor)level, (BlockPos)blockPos, null);
            level.removeBlock(blockPos, false);
        }
    }

    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (blockState.getBlock() == Ic2Blocks.RESIN_SHEET) {
            entity.fallDistance = (float)((double)entity.fallDistance * 0.75);
            entity.setDeltaMovement(entity.getDeltaMovement().x() * 0.6, entity.getDeltaMovement().y() * 0.85, entity.getDeltaMovement().z() * 0.6);
        } else if (blockState.getBlock() == Ic2Blocks.RUBBER_SHEET) {
            if (!level.isEmptyBlock(blockPos.below())) {
                return;
            }
            if (entity instanceof LivingEntity && !Ic2SheetBlock.canSupportWeight(level, blockPos)) {
                level.levelEvent(2001, blockPos, Block.getId((BlockState)blockState));
                level.removeBlock(blockPos, false);
                return;
            }
            if (entity.getDeltaMovement().y() <= -0.4) {
                entity.fallDistance = 0.0f;
                entity.setDeltaMovement(entity.getDeltaMovement().x() * 1.1, entity.getDeltaMovement().y(), entity.getDeltaMovement().z() * 1.1);
                if (entity instanceof LivingEntity) {
                    if (entity instanceof Player && IC2.keyboard.isJumpKeyDown((Player)entity)) {
                        entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -1.3, entity.getDeltaMovement().z());
                    } else if (entity instanceof Player && entity.isShiftKeyDown()) {
                        entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.1, entity.getDeltaMovement().z());
                    } else {
                        entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.8, entity.getDeltaMovement().z());
                    }
                } else {
                    entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.8, entity.getDeltaMovement().z());
                }
            } else if (blockState.getBlock() == Ic2Blocks.WOOL_SHEET) {
                entity.fallDistance = (float)((double)entity.fallDistance * 0.95);
            }
        }
    }

    private static boolean canSupportWeight(Level level, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block0: for (Direction direction : positiveHorizontalFacings) {
            for (int i = -1; i <= 1; i += 2) {
                mutableBlockPos.set((Vec3i)blockPos);
                boolean bl = false;
                for (int j = 0; j < 16; ++j) {
                    mutableBlockPos.move(direction, i);
                    BlockState blockState = level.getBlockState((BlockPos)mutableBlockPos);
                    if (blockState.isCollisionShapeFullBlock(level, (BlockPos)mutableBlockPos)) {
                        bl = true;
                        break;
                    }
                    if (blockState != Ic2Blocks.RUBBER_SHEET.defaultBlockState()) break;
                    mutableBlockPos.move(Direction.DOWN);
                    BlockState blockState2 = level.getBlockState((BlockPos)mutableBlockPos);
                    if (blockState2.isCollisionShapeFullBlock(level, (BlockPos)mutableBlockPos)) {
                        bl = true;
                        break;
                    }
                    mutableBlockPos.move(Direction.UP);
                }
                if (!bl) continue block0;
                if (i != 1) continue;
                return true;
            }
        }
        return false;
    }
}

