/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.damagesource.IndirectEntityDamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrowableProjectile
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.SmeltingRecipe
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Explosion$BlockInteraction
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.StainedGlassBlock
 *  net.minecraft.world.level.block.StainedGlassPaneBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 */
package ic2.core.entity;

import ic2.core.IC2;
import ic2.core.Ic2Explosion;
import ic2.core.Ic2Player;
import ic2.core.ref.Ic2Entities;
import ic2.core.util.StackUtil;
import ic2.core.util.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LaserBulletEntity
extends ThrowableProjectile {
    public static final double laserSpeed = 1.0;
    public LivingEntity owner;
    public boolean isSmeltMode = false;
    public boolean removeBlock = false;
    public float range = 0.0f;
    public float power = 0.0f;
    public int blockBreaks = 0;
    public boolean isExplosiveMode = false;

    public LaserBulletEntity(Level level) {
        super(Ic2Entities.LASER_BULLET, level);
    }

    public LaserBulletEntity(EntityType<? extends LaserBulletEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LaserBulletEntity(Level level, LivingEntity livingEntity) {
        super(Ic2Entities.LASER_BULLET, livingEntity, level);
    }

    public LaserBulletEntity(Level level, Vector3 vector3, Vector3 vector32, LivingEntity livingEntity, float f, float f2, int n, boolean bl) {
        this(level, livingEntity);
        this.owner = livingEntity;
        this.absMoveTo(vector3.x, vector3.y, vector3.z);
        this.range = f;
        this.power = f2;
        this.blockBreaks = n;
        this.isExplosiveMode = bl;
    }

    protected double getDefaultGravity() {
        return 0.0;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    public void tick() {
        super.tick();
        if (IC2.sideProxy.isSimulating() && (this.range < 1.0f || this.power <= 0.0f || this.blockBreaks <= 0)) {
            if (this.isExplosiveMode) {
                this.explode();
            }
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        this.power -= 0.5f;
    }

    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        this.handleHit((HitResult)blockHitResult);
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        this.handleHit((HitResult)entityHitResult);
    }

    protected void handleHit(HitResult hitResult) {
        if (this.isExplosiveMode) {
            this.explode();
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        switch (hitResult.getType()) {
            case ENTITY: {
                if (this.hitEntity(((EntityHitResult)hitResult).getEntity())) {
                    this.power -= 0.5f;
                    break;
                }
                this.remove(Entity.RemovalReason.DISCARDED);
                break;
            }
            case BLOCK: {
                assert (hitResult instanceof BlockHitResult);
                BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                if (!this.hitBlock(blockHitResult.getBlockPos(), blockHitResult.getDirection())) {
                    this.power -= 0.5f;
                    break;
                }
                this.remove(Entity.RemovalReason.DISCARDED);
                break;
            }
            default: {
                throw new RuntimeException("invalid hit type: " + hitResult.getType());
            }
        }
    }

    private void explode() {
        Level level = this.getCommandSenderWorld();
        Ic2Explosion ic2Explosion = new Ic2Explosion(level, (Entity)this, this.getX(), this.getY(), this.getZ(), 5.0f, 0.85f);
        ic2Explosion.doExplosion();
    }

    private boolean hitEntity(Entity entity) {
        int n = (int)this.power;
        if (n > 0) {
            entity.igniteForSeconds((float)(n * (this.isSmeltMode ? 2 : 1)));
            return entity.hurt(this.damageSources().indirectMagic(this, this.owner), (float)n);
        }
        return true;
    }

    private boolean hitBlock(BlockPos blockPos, Direction direction) {
        Player player;
        Level level = this.getCommandSenderWorld();
        Player player2 = player = this.owner instanceof Player ? (Player)this.owner : Ic2Player.get(level);
        if (player == null) {
            return false;
        }
        if (player.blockActionRestricted(level, blockPos, ((net.minecraft.server.level.ServerPlayer)player).gameMode.getGameModeForPlayer())) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        boolean bl = true;
        if (level.getBlockState(blockPos).isAir() || block == Blocks.GLASS || block == Blocks.GLASS_PANE || block instanceof StainedGlassPaneBlock || block instanceof StainedGlassBlock) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        float f = blockState.getDestroySpeed((BlockGetter)level, blockPos);
        if (f < 0.0f) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return true;
        }
        this.power -= f / 1.5f;
        if (this.power < 0.0f) {
            return true;
        }
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        if (blockState.getMapColor(null, null) == MapColor.COLOR_RED) {
            block.wasExploded(level, blockPos, new Explosion(level, (Entity)this, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 1.0f, false, Explosion.BlockInteraction.DESTROY));
        } else if (this.isSmeltMode) {
            if (blockState.getMapColor(null, null) == MapColor.WOOD) {
                bl = false;
            } else {
                for (ItemStack itemStack : StackUtil.getDrops((BlockGetter)level, blockPos, blockState, block, 0)) {
                    this.appendSmeltItemStack(block, itemStack, arrayList);
                }
                bl = arrayList.isEmpty();
            }
        }
        if (this.removeBlock) {
            if (bl) {
                Block.dropResources((BlockState)blockState, (Level)level, (BlockPos)blockPos);
            }
            level.removeBlock(blockPos, false);
            for (ItemStack itemStack : arrayList) {
                if (!StackUtil.placeBlock(itemStack, level, blockPos)) {
                    StackUtil.dropAsEntity(level, blockPos, itemStack);
                }
                this.power = 0.0f;
            }
            if (level.random.nextInt(10) == 0 && blockState.is(BlockTags.LOGS_THAT_BURN)) {
                level.setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
            }
        }
        --this.blockBreaks;
        return true;
    }

    private void appendSmeltItemStack(Block block, ItemStack itemStack, List<ItemStack> list) {
        SmeltingRecipe smeltingRecipe;
        if (itemStack.getItem() instanceof BlockItem && ((BlockItem)itemStack.getItem()).getBlock() != block) {
            itemStack = new ItemStack((ItemLike)block.asItem());
        }
        if ((smeltingRecipe = IC2.sideProxy.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(itemStack), this.level()).map(net.minecraft.world.item.crafting.RecipeHolder::value).orElse(null)) == null) {
            return;
        }
        ItemStack itemStack2 = smeltingRecipe.getResultItem(this.level().registryAccess());
        if (!StackUtil.isEmpty(itemStack2)) {
            list.add(itemStack2);
        }
    }

    public void init(LivingEntity livingEntity, float f, float f2, int n, boolean bl, boolean bl2, boolean bl3) {
        this.owner = livingEntity;
        this.range = f;
        this.power = f2;
        this.blockBreaks = n;
        this.removeBlock = bl3;
        this.isExplosiveMode = bl;
        this.isSmeltMode = bl2;
    }
}

