package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.Ic2Explosion;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Quaternion;
import ic2.core.util.Vector3;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 等离子粒子实体（ex112 兼容版，适配 1.21.1）：
 * - 1.21.1 Entity 构造需 EntityType（Ic2Entities.PLASMA_PARTICLE）
 * - onUpdate -> tick；射线用 ClipContext；碰撞盒用 AABB.clip
 * - onInfluence 的熔炼逻辑简化（1.21.1 RecipeManager 复杂，只保留水/火焰处理）
 */
public class EntityParticle
extends Entity {
    private double coreSize;
    private double influenceSize;
    private int lifeTime;
    private Entity owner;
    private Vector3[] radialTestVectors;

    public EntityParticle(EntityType<? extends EntityParticle> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.lifeTime = 6000;
    }

    public EntityParticle(EntityType<? extends EntityParticle> type, Level world, LivingEntity owner1, float speed, double coreSize1, double influenceSize1) {
        this(type, world);
        this.coreSize = coreSize1;
        this.influenceSize = influenceSize1;
        this.owner = owner1;
        Vector3 eyePos = Util.getEyePosition(this.owner);
        this.setPos(eyePos.x, eyePos.y, eyePos.z);
        Vector3 motion = new Vector3(owner1.getViewVector(1.0F));
        Vector3 ortho = motion.copy().cross(Vector3.UP).scaleTo(influenceSize1);
        double stepAngle = Math.atan(0.5 / influenceSize1) * 2.0;
        int steps = (int)Math.ceil(Math.PI * 2 / stepAngle);
        Quaternion q = new Quaternion().setFromAxisAngle(motion, stepAngle);
        this.radialTestVectors = new Vector3[steps];
        this.radialTestVectors[0] = ortho.copy();
        for (int i = 1; i < steps; ++i) {
            q.rotate(ortho);
            this.radialTestVectors[i] = ortho.copy();
        }
        motion.scale(speed);
        this.setDeltaMovement(motion.x, motion.y, motion.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    public Entity getThrower() {
        return this.owner;
    }

    public void setThrower(Entity entity) {
        this.owner = entity;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        Vec3 start = this.position();
        Vec3 end = start.add(motion);
        this.setPos(end.x, end.y, end.z);
        Level world = this.level();
        HitResult hit = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        if (hit != null && hit.getType() != HitResult.Type.MISS) {
            end = hit.getLocation();
            this.setPos(end.x, end.y, end.z);
        }
        AABB moveBox = new AABB(start, end).inflate(this.influenceSize);
        List<Entity> entitiesToCheck = world.getEntities(this, moveBox, entity -> entity != this.owner && entity.isPickable());
        ArrayList<HitResult> entitiesInfluences = new ArrayList<HitResult>();
        double minDistanceSq = start.distanceToSqr(end);
        for (Entity entity : entitiesToCheck) {
            Vec3 influenceVec = entity.getBoundingBox().inflate(this.influenceSize).clip(start, end).orElse(null);
            HitResult entityInfluence = influenceVec == null ? null : new net.minecraft.world.phys.BlockHitResult(influenceVec, net.minecraft.core.Direction.UP, net.minecraft.core.BlockPos.containing(influenceVec), false);
            if (entityInfluence == null) {
                continue;
            }
            entitiesInfluences.add(entityInfluence);
            Vec3 entityHit = entity.getBoundingBox().inflate(this.coreSize).clip(start, end).orElse(null);
            if (entityHit == null) {
                continue;
            }
            double distanceSq = start.distanceToSqr(entityHit);
            if (!(distanceSq < minDistanceSq)) {
                continue;
            }
            hit = new net.minecraft.world.phys.BlockHitResult(entityHit, net.minecraft.core.Direction.UP, net.minecraft.core.BlockPos.containing(entityHit), false);
            minDistanceSq = distanceSq;
        }
        double maxInfluenceDistance = Math.sqrt(minDistanceSq) + this.influenceSize;
        for (HitResult entityInfluence : entitiesInfluences) {
            if (start.distanceTo(entityInfluence.getLocation()) <= maxInfluenceDistance) {
                this.onInfluence(entityInfluence);
            }
        }
        if (this.radialTestVectors != null) {
            Vector3 vForward = new Vector3(end).sub(new Vector3(start));
            double len = vForward.length();
            vForward.scale(1.0 / len);
            Vector3 origin = new Vector3(start);
            Vector3 tmp = new Vector3();
            int d = 0;
            while ((double)d < len) {
                for (Vector3 radialTestVector : this.radialTestVectors) {
                    origin.copy(tmp).add(radialTestVector);
                    HitResult influence = world.clip(new ClipContext(origin.toVec3(), tmp.toVec3(), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
                    if (influence != null) {
                        this.onInfluence(influence);
                    }
                }
                origin.add(vForward);
                ++d;
            }
        }
        if (hit != null && hit.getType() != HitResult.Type.MISS) {
            this.onImpact(hit);
            this.discard();
        } else {
            --this.lifeTime;
            if (this.lifeTime <= 0) {
                this.discard();
            }
        }
    }

    protected void onImpact(HitResult hit) {
        if (!IC2.platform.isSimulating()) {
            return;
        }
        Ic2Explosion explosion = new Ic2Explosion(this.level(), this.owner, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 18.0f, 0.95f, Ic2Explosion.Type.Heat);
        explosion.doExplosion();
    }

    protected void onInfluence(HitResult hit) {
        if (!IC2.platform.isSimulating() || !(hit instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult blockHit = (BlockHitResult)hit;
        Level world = this.level();
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.WATER) {
            world.removeBlock(pos, false);
        } else if (block.isFlammable(state, world, pos, blockHit.getDirection())) {
            world.setBlockAndUpdate(pos.relative(blockHit.getDirection().getOpposite()), Blocks.FIRE.defaultBlockState());
        }
    }
}
