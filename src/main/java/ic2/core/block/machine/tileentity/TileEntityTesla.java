/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.EntitySelector
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package ic2.core.block.machine.tileentity;

import org.joml.Vector3f;
import ic2.core.IC2;
import ic2.core.Ic2DamageSource;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.ref.Ic2BlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileEntityTesla
extends Ic2TileEntity {
    private static final DustParticleOptions effect = new DustParticleOptions(new Vector3f(0.1f, 0.1f, 1.0f), 1.0f);
    protected final Redstone redstone;
    protected final Energy energy;
    private int ticker = IC2.random.nextInt(32);

    public TileEntityTesla(BlockPos blockPos, BlockState blockState) {
        super(Ic2BlockEntities.TESLA_COIL, blockPos, blockState);
        this.redstone = this.addComponent(new Redstone(this));
        this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 2));
    }

    @Override
    protected void updateEntityServer() {
        int n;
        super.updateEntityServer();
        if (!this.redstone.hasRedstoneInput()) {
            return;
        }
        if (this.energy.useEnergy(1.0) && ++this.ticker % 32 == 0 && (n = (int)this.energy.getEnergy() / 400) > 0 && this.shock(n)) {
            // 第三十八轮：删掉迁移时留下的调试输出（System.out.println(n)）
            this.energy.useEnergy(n * 400);
        }
    }

    protected boolean shock(int n) {
        Level level = this.getLevel();
        if (level == null) {
            return false;
        }
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB((double)(this.worldPosition.getX() - 4), (double)(this.worldPosition.getY() - 4), (double)(this.worldPosition.getZ() - 4), (double)(this.worldPosition.getX() + 4 + 1), (double)(this.worldPosition.getY() + 4 + 1), (double)(this.worldPosition.getZ() + 4 + 1)), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (list.size() == 0) {
            return false;
        }
        boolean bl = false;
        int n2 = n / list.size();
        for (LivingEntity livingEntity : list) {
            if (ItemArmorHazmat.hasCompleteHazmat(livingEntity) || !livingEntity.hurt(Ic2DamageSource.electricity(level), (float)n2)) continue;
            // 第三十八轮：1.12.2 的 `getZapped`（"我感受到电流 / 忘记绝缘"）没有任何触发点，这里补上 ——
            // 被特斯拉线圈电到（未穿整套防化服）的玩家发放成就。
            if (livingEntity instanceof Player) {
                IC2.achievements.issueAchievement((Player)livingEntity, "getZapped");
            }
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                RandomSource randomSource = level.random;
                for (int i = 0; i < n2; ++i) {
                    serverLevel.addParticle((ParticleOptions)effect, livingEntity.getX() + (double)randomSource.nextFloat() - 0.5, livingEntity.getY() + (double)(randomSource.nextFloat() * 2.0f) - 1.0, livingEntity.getZ() + (double)randomSource.nextFloat() - 0.5, 0.0, 0.0, 0.0);
                }
            }
            bl = true;
        }
        return bl;
    }
}

