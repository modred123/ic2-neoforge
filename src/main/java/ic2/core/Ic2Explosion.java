/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntitySelector
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Explosion$BlockInteraction
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.PathNavigationRegion
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.storage.loot.LootContext$Builder
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core;

import ic2.api.tile.ExplosionWhitelist;
import ic2.core.IC2;
import ic2.core.Ic2DamageSource;
import ic2.core.Ic2Potion;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ic2Explosion
extends Explosion {
    private final Level worldObj;
    private final Entity exploder;
    private final double explosionX;
    private final double explosionY;
    private final double explosionZ;
    private final int worldMaxHeight;
    private final int worldMinHeight;
    private final float power;
    private final float explosionDropRate;
    private final Type type;
    private final int radiationRange;
    private final LivingEntity igniter;
    private final Random rng = new Random();
    private final double maxDistance;
    private final int areaSize;
    private final int areaX;
    private final int areaZ;
    private final DamageSource damageSource;
    private final List<EntityDamage> entitiesInRange = new ArrayList<EntityDamage>();
    private final long[][] destroyedBlockPositions;
    private BlockGetter chunkCache;
    private static final double dropPowerLimit = 8.0;
    private static final double damageAtDropPowerLimit = 32.0;
    private static final double accelerationAtDropPowerLimit = 0.7;
    private static final double motionLimit = 60.0;
    private static final int secondaryRayCount = 5;
    private static final int bitSetElementSize = 2;

    public Ic2Explosion(Level level, Entity entity, double d, double d2, double d3, float f, float f2) {
        this(level, entity, d, d2, d3, f, f2, Type.Normal);
    }

    public Ic2Explosion(Level level, Entity entity, double d, double d2, double d3, float f, float f2, Type type) {
        this(level, entity, d, d2, d3, f, f2, type, null, 0);
    }

    public Ic2Explosion(Level level, Entity entity, BlockPos blockPos, float f, float f2, Type type) {
        this(level, entity, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, f, f2, type);
    }

    public Ic2Explosion(Level level, Entity entity, double d, double d2, double d3, float f, float f2, Type type, LivingEntity livingEntity, int n) {
        super(level, entity, d, d2, d3, f, true, Explosion.BlockInteraction.DESTROY);
        this.worldObj = level;
        this.exploder = entity;
        this.explosionX = d;
        this.explosionY = d2;
        this.explosionZ = d3;
        this.worldMaxHeight = IC2.getWorldMaxHeight(level);
        this.worldMinHeight = IC2.getWorldMinHeight(level);
        this.power = f;
        this.explosionDropRate = f2;
        this.type = type;
        this.igniter = livingEntity;
        this.radiationRange = n;
        this.maxDistance = (double)f / 0.4;
        int n2 = (int)Math.ceil(this.maxDistance);
        this.areaSize = n2 * 2;
        this.areaX = Util.roundToNegInf(d) - n2;
        this.areaZ = Util.roundToNegInf(d3) - n2;
        // 第三十七轮修复（用户实测：低压电炉接 MFSU 过压 → 崩服）：
        // 过压爆炸经 `EnergyCalculatorLeg.explodeTile` → `new Ic2Explosion(level, null, …)`，
        // exploder 与 igniter **都是 null**；旧代码用 `livingEntity.damageSources()`（实例方法）取伤害源 → NPE
        // （crash-2026-10-06_19.09.17-server.txt:7 `Cannot invoke "LivingEntity.damageSources()" because "livingEntity" is null`）。
        // 1.12.2 权威是 `DamageSource.causeExplosionDamage(this)`（只依赖 explosion 自身），
        // 1.19.2 上游是 `DamageSource.explosion((LivingEntity) null)`（静态方法，同样允许 null）。
        // 1.21 的等价写法：由 level 取 DamageSources（level 必非 null），cause 传 igniter ——
        // null 时自动降级为通用 explosion 伤害源，与 1.12.2 语义一致。
        this.damageSource = this.isNuclear()
                ? Ic2DamageSource.getNukeSource(level, livingEntity)
                : level.damageSources().explosion(livingEntity, livingEntity);
        this.destroyedBlockPositions = new long[this.worldMaxHeight - this.worldMinHeight][];
    }

    public Ic2Explosion(Level level, Entity entity, BlockPos blockPos, int n, float f, Type type) {
        this(level, entity, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, n, f, type);
    }

    public void doExplosion() {
        Block block;
        int n;
        Object object;
        double d;
        boolean bl;
        if (this.power <= 0.0f) {
            return;
        }
        Vec3 vec3 = new Vec3(this.explosionX, this.explosionY, this.explosionZ);
        if (!IC2.envProxy.announceExplosion(this.worldObj, this.exploder, vec3, this.power, this.igniter, this.radiationRange, this.maxDistance)) {
            return;
        }
        int n2 = this.areaSize / 2;
        BlockPos blockPos = BlockPos.containing(this.explosionX, this.explosionY, this.explosionZ);
        BlockPos blockPos2 = blockPos.offset(-n2, -n2, -n2);
        BlockPos blockPos3 = blockPos.offset(n2, n2, n2);
        this.chunkCache = new PathNavigationRegion(this.worldObj, blockPos2, blockPos3);
        List<Entity> list = this.worldObj.getEntities(this.exploder, new AABB(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), blockPos3.getX() + 1, blockPos3.getY() + 1, blockPos3.getZ() + 1));
        for (Entity entity : list) {
            if (!(entity instanceof LivingEntity) && !(entity instanceof ItemEntity)) continue;
            int n3 = (int)(Util.square(entity.getX() - this.explosionX) + Util.square(entity.getY() - this.explosionY) + Util.square(entity.getZ() - this.explosionZ));
            double d2 = Ic2Explosion.getEntityHealth(entity);
            this.entitiesInRange.add(new EntityDamage(entity, n3, d2));
        }
        boolean bl2 = bl = !this.entitiesInRange.isEmpty();
        if (bl) {
            this.entitiesInRange.sort(Comparator.comparingInt(entityDamage -> entityDamage.distance));
        }
        int n4 = (int)Math.ceil(Math.PI / Math.atan(1.0 / this.maxDistance));
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 2 * n4; ++i) {
            for (int j = 0; j < n4; ++j) {
                double d3 = Math.PI * 2 / (double)n4 * (double)i;
                double d4 = Math.PI / (double)n4 * (double)j;
                this.shootRay(this.explosionX, this.explosionY, this.explosionZ, d3, d4, this.power, bl && i % 8 == 0 && j % 8 == 0, mutableBlockPos);
            }
        }
        for (EntityDamage entityDamage : this.entitiesInRange) {
            Entity entity = entityDamage.entity;
            entity.hurt(this.damageSource, (float)entityDamage.damage);
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (this.isNuclear() && this.igniter != null && player == this.igniter && player.getHealth() <= 0.0f) {
                    IC2.achievements.issueAchievement(player, "dieFromOwnNuke");
                }
            }
            double d5 = (d = Util.square(entityDamage.motionX) + Util.square(entityDamage.motionY) + Util.square(entityDamage.motionZ)) > 3600.0 ? Math.sqrt(3600.0 / d) : 1.0;
            entity.setDeltaMovement(entity.getDeltaMovement().add(entityDamage.motionX * d5, entityDamage.motionY * d5, entityDamage.motionZ * d5));
        }
        if (this.isNuclear() && this.radiationRange >= 1) {
            List<Mob> mobs = this.worldObj.getEntitiesOfClass(Mob.class, new AABB(this.explosionX - (double)this.radiationRange, this.explosionY - (double)this.radiationRange, this.explosionZ - (double)this.radiationRange, this.explosionX + (double)this.radiationRange, this.explosionY + (double)this.radiationRange, this.explosionZ + (double)this.radiationRange), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            Iterator<Mob> iterator = mobs.iterator();
            while (iterator.hasNext()) {
                Mob mob = (Mob)iterator.next();
                if (ItemArmorHazmat.hasCompleteHazmat((LivingEntity)mob)) continue;
                d = mob.position().distanceTo(vec3);
                int n5 = (int)(120.0 * ((double)this.radiationRange - d));
                int n6 = (int)(80.0 * ((double)(this.radiationRange / 3) - d));
                if (n5 >= 0) {
                    mob.addEffect(new MobEffectInstance(MobEffects.HUNGER, n5, 0));
                }
                if (n6 < 0) continue;
                Ic2Potion.radiation.applyTo((LivingEntity)mob, n6, 0);
            }
        }
        IC2.network.get(true).initiateExplosionEffect(this.worldObj, vec3, this.type);
        RandomSource random = this.worldObj.random;
        boolean bl3 = this.worldObj.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
        HashMap<XZPosition, HashMap<ItemComparableItemStack, DropData>> hashMap = new HashMap<XZPosition, HashMap<ItemComparableItemStack, DropData>>();
        // 第四十九轮修复（用户实测：核爆后除红石火把外没有任何掉落，像"直接清空区域"）：
        // 原实现走 1.21 的战利品表路径 `blockState.getDrops(builder)`，而该 builder 的 `TOOL` 是
        // `ItemStack.EMPTY` —— **空手** 判定 → 石头/矿石等需要镐的方块一律掉落为空。1.12.2 的
        // 权威实现是 `StackUtil.getDrops(worldObj, pos, state, block, 0)`（`ExplosionIC2.java:221`），
        // `Block.getDrops(...)` 在 1.12.2 里**不看工具**（只有时运等级）→ 所有方块都按默认掉落。
        // 现改回同一条路径（`StackUtil.getDrops` 内部用钻石镐，等价于 1.12.2 的"无视工具"语义）。
        int diagDropped = 0;
        int diagEntities = 0;
        for (int i = 0; i < this.destroyedBlockPositions.length; ++i) {
            int n7 = i + this.worldMinHeight;
            long[] lArray = this.destroyedBlockPositions[i];
            if (lArray == null) continue;
            int n8 = -2;
            while ((n8 = Ic2Explosion.nextSetIndex(n8 + 2, lArray, 2)) != -1) {
                int n9 = n8 / 2;
                int n10 = n9 / this.areaSize;
                n = n9 - n10 * this.areaSize;
                mutableBlockPos.set(n += this.areaX, n7, n10 += this.areaZ);
                BlockState blockState = this.chunkCache.getBlockState((BlockPos)mutableBlockPos);
                block = blockState.getBlock();
                if (this.power < 20.0f) {
                    // empty if block
                }
                if (bl3 && block.dropFromExplosion((Explosion)this) && Ic2Explosion.getAtIndex(n8, lArray, 2) == 1) {
                    List<ItemStack> list2 = StackUtil.getDrops(this.worldObj, (BlockPos)mutableBlockPos, blockState, 0);
                    for (ItemStack itemStack : list2) {
                        ItemComparableItemStack itemComparableItemStack;
                        DropData dropData;
                        if (random.nextFloat() > this.explosionDropRate) continue;
                        XZPosition xZPosition = new XZPosition(n / 2, n10 / 2);
                        HashMap<ItemComparableItemStack, DropData> hashMap2 = (HashMap<ItemComparableItemStack, DropData>)hashMap.get(xZPosition);
                        if (hashMap2 == null) {
                            hashMap2 = new HashMap<ItemComparableItemStack, DropData>();
                            hashMap.put(xZPosition, hashMap2);
                        }
                        if ((dropData = (DropData)hashMap2.get(itemComparableItemStack = new ItemComparableItemStack(itemStack, false))) == null) {
                            dropData = new DropData(StackUtil.getSize(itemStack), n7);
                            hashMap2.put(itemComparableItemStack.copy(), dropData);
                            diagDropped += StackUtil.getSize(itemStack);
                            continue;
                        }
                        dropData.add(StackUtil.getSize(itemStack), n7);
                        diagDropped += StackUtil.getSize(itemStack);
                    }
                }
                this.worldObj.setBlock((BlockPos)mutableBlockPos, Blocks.AIR.defaultBlockState(), 3);
                block.wasExploded(this.worldObj, (BlockPos)mutableBlockPos, (Explosion)this);
            }
        }
        for (Map.Entry<XZPosition, HashMap<ItemComparableItemStack, DropData>> entry : hashMap.entrySet()) {
            XZPosition xZPosition = entry.getKey();
            for (Map.Entry<ItemComparableItemStack, DropData> entry2 : entry.getValue().entrySet()) {
                int n11;
                ItemComparableItemStack itemComparableItemStack = entry2.getKey();
                for (n = ((DropData)entry2.getValue()).n; n > 0; n -= n11) {
                    n11 = Math.min(n, 64);
                    ItemEntity itemEntity = new ItemEntity(this.worldObj, (double)(((float)xZPosition.x + this.worldObj.random.nextFloat()) * 2.0f), (double)((DropData)entry2.getValue()).maxY + 0.5, (double)(((float)xZPosition.z + this.worldObj.random.nextFloat()) * 2.0f), itemComparableItemStack.toStack(n11));
                    itemEntity.setDefaultPickUpDelay();
                    this.worldObj.addFreshEntity(itemEntity);
                    ++diagEntities;
                }
            }
        }
        // 第四十九轮诊断：大爆炸的掉落统计（droppedStacks=0 即"清空区域"现象复现）
        if (this.power >= 20.0f) {
            IC2.log.info(ic2.core.util.LogCategory.General, "[ExplosionDiag] power=%.1f dropRate=%.3f doDrops=%b droppedStacks=%d entities=%d", this.power, this.explosionDropRate, bl3, diagDropped, diagEntities);
        }
    }

    public void destroy(int n, int n2, int n3, boolean bl) {
        this.destroyUnchecked(n, n2, n3, bl);
    }

    private void destroyUnchecked(int n, int n2, int n3, boolean bl) {
        int n4 = (n3 - this.areaZ) * this.areaSize + (n - this.areaX);
        n4 *= 2;
        long[] lArray = this.destroyedBlockPositions[n2 - this.worldMinHeight];
        if (lArray == null) {
            lArray = Ic2Explosion.makeArray(Util.square(this.areaSize), 2);
            this.destroyedBlockPositions[n2 - this.worldMinHeight] = lArray;
        }
        if (bl) {
            Ic2Explosion.setAtIndex(n4, lArray, 3);
        } else {
            Ic2Explosion.setAtIndex(n4, lArray, 1);
        }
    }

    private void shootRay(double d, double d2, double d3, double d4, double d5, double d6, boolean bl, BlockPos.MutableBlockPos mutableBlockPos) {
        int n;
        double d7 = Math.sin(d5) * Math.cos(d4);
        double d8 = Math.cos(d5);
        double d9 = Math.sin(d5) * Math.sin(d4);
        int n2 = 0;
        while ((n = Util.roundToNegInf(d2)) >= this.worldMinHeight && n < this.worldMaxHeight) {
            int n3 = Util.roundToNegInf(d);
            int n4 = Util.roundToNegInf(d3);
            mutableBlockPos.set(n3, n, n4);
            BlockState blockState = this.chunkCache.getBlockState((BlockPos)mutableBlockPos);
            Block block = blockState.getBlock();
            double d10 = this.getAbsorption(blockState, (BlockPos)mutableBlockPos);
            if (d10 < 0.0) break;
            if (d10 > 1000.0 && !ExplosionWhitelist.isBlockWhitelisted(block)) {
                d10 = 0.5;
            } else {
                if (d10 > d6) break;
                if (block == Blocks.STONE || block != Blocks.AIR && !blockState.isAir()) {
                    this.destroyUnchecked(n3, n, n4, d6 > 8.0);
                }
            }
            if (bl && (n2 + 4) % 8 == 0 && !this.entitiesInRange.isEmpty() && d6 >= 0.25) {
                this.damageEntities(d, d2, d3, n2, d6);
            }
            if (d10 > 10.0) {
                for (int i = 0; i < 5; ++i) {
                    this.shootRay(d, d2, d3, this.rng.nextDouble() * 2.0 * Math.PI, this.rng.nextDouble() * Math.PI, d10 * 0.4, false, mutableBlockPos);
                }
            }
            d6 -= d10;
            d += d7;
            d2 += d8;
            d3 += d9;
            ++n2;
        }
    }

    private double getAbsorption(BlockState blockState, BlockPos blockPos) {
        double d = 0.5;
        Block block = blockState.getBlock();
        if (block == Blocks.AIR || blockState.isAir()) {
            return d;
        }
        if (block == Blocks.WATER && this.type != Type.Normal) {
            d += 1.0;
        } else {
            float f = IC2.envProxy.getBlastResistance(blockState, (BlockGetter)this.worldObj, blockPos, this);
            if (f < 0.0f) {
                return f;
            }
            double d2 = (double)(f + 4.0f) * 0.3;
            d = this.type != Type.Heat ? (d += d2) : (d += d2 * 6.0);
        }
        return d;
    }

    private void damageEntities(double d, double d2, double d3, int n, double d4) {
        int n2;
        int var10_11;
        if (n != 4) {
            n2 = Util.square(n - 5);
            int n3 = 0;
            int n4 = this.entitiesInRange.size() - 1;
            do {
                var10_11 = (n3 + n4) / 2;
                int n5 = this.entitiesInRange.get((int)var10_11).distance;
                if (n5 < n2) {
                    n3 = var10_11 + 1;
                    continue;
                }
                n4 = n5 > n2 ? var10_11 - 1 : var10_11;
            } while (n3 < n4);
        } else {
            var10_11 = 0;
        }
        int n6 = Util.square(n + 5);
        for (n2 = var10_11; n2 < this.entitiesInRange.size(); ++n2) {
            EntityDamage entityDamage = this.entitiesInRange.get(n2);
            if (entityDamage.distance >= n6) break;
            Entity entity = entityDamage.entity;
            if (!(Util.square(entity.getX() - d) + Util.square(entity.getY() - d2) + Util.square(entity.getZ() - d3) <= 25.0)) continue;
            double d5 = 4.0 * d4;
            entityDamage.damage += d5;
            entityDamage.health -= d5;
            double d6 = entity.getX() - this.explosionX;
            double d7 = entity.getY() - this.explosionY;
            double d8 = entity.getZ() - this.explosionZ;
            double d9 = Math.sqrt(d6 * d6 + d7 * d7 + d8 * d8);
            entityDamage.motionX += d6 / d9 * 0.0875 * d4;
            entityDamage.motionY += d7 / d9 * 0.0875 * d4;
            entityDamage.motionZ += d8 / d9 * 0.0875 * d4;
            if (!(entityDamage.health <= 0.0)) continue;
            entity.hurt(this.damageSource, (float)entityDamage.damage);
            if (entity.isAlive()) continue;
            this.entitiesInRange.remove(n2);
            --n2;
        }
    }

    public LivingEntity getSourceMob() {
        return this.igniter;
    }

    private boolean isNuclear() {
        return this.type == Type.Nuclear;
    }

    private static double getEntityHealth(Entity entity) {
        if (entity instanceof ItemEntity) {
            return 5.0;
        }
        return Double.POSITIVE_INFINITY;
    }

    private static long[] makeArray(int n, int n2) {
        return new long[(n * n2 + 8 - n2) / 8];
    }

    private static int nextSetIndex(int n, long[] lArray, int n2) {
        int n3 = n % 8;
        for (int i = n / 8; i < lArray.length; ++i) {
            long l = lArray[i];
            for (int j = n3; j < 8; j += n2) {
                int n4 = (int)(l >> j & (long)((1 << n2) - 1));
                if (n4 == 0) continue;
                return i * 8 + j;
            }
            n3 = 0;
        }
        return -1;
    }

    private static int getAtIndex(int n, long[] lArray, int n2) {
        return (int)(lArray[n / 8] >>> n % 8 & (long)((1 << n2) - 1));
    }

    private static void setAtIndex(int n, long[] lArray, int n2) {
        int n3 = n / 8;
        lArray[n3] = lArray[n3] | (long)(n2 << n % 8);
    }


    public static enum Type {
        Normal,
        Heat,
        Electrical,
        Nuclear;

    }

    private static class EntityDamage {
        final Entity entity;
        final int distance;
        double health;
        double damage;
        double motionX;
        double motionY;
        double motionZ;

        EntityDamage(Entity entity, int n, double d) {
            this.entity = entity;
            this.distance = n;
            this.health = d;
        }
    }

    private static class XZPosition {
        int x;
        int z;

        XZPosition(int n, int n2) {
            this.x = n;
            this.z = n2;
        }

        public boolean equals(Object object) {
            if (object instanceof XZPosition) {
                XZPosition xZPosition = (XZPosition)object;
                return xZPosition.x == this.x && xZPosition.z == this.z;
            }
            return false;
        }

        public int hashCode() {
            return this.x * 31 ^ this.z;
        }
    }

    private static class DropData {
        int n;
        int maxY;

        DropData(int n, int n2) {
            this.n = n;
            this.maxY = n2;
        }

        public DropData add(int n, int n2) {
            this.n += n;
            if (n2 > this.maxY) {
                this.maxY = n2;
            }
            return this;
        }
    }
}

