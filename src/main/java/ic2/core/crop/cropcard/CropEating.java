/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 */
package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.api.item.ItemWrapper;
import ic2.core.IC2;
import ic2.core.Ic2DamageSource;
import ic2.core.crop.Ic2CropCard;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.BiomeUtil;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class CropEating
extends Ic2CropCard {
    private final double movementMultiplier = 0.5;
    private final double length = 1.0;

    public CropEating(ICropType iCropType) {
        super(iCropType);
    }

    @Override
    public Block getCropBlock() {
        return Ic2Blocks.EATING_PLANT_CROP;
    }

    @Override
    public String getDiscoveredBy() {
        return "Hasudako";
    }

    @Override
    public CropProperties getProperties() {
        return new CropProperties(6, 1, 1, 3, 1, 4);
    }

    @Override
    public String[] getAttributes() {
        return new String[]{"Bad", "Food"};
    }

    @Override
    public boolean canGrow(ICropTile iCropTile) {
        if (iCropTile.getCurrentAge() < 2) {
            return iCropTile.getLightLevel() > 10;
        }
        return iCropTile.isBlockBelow(Blocks.LAVA) && iCropTile.getCurrentAge() < this.getMaxAge() && iCropTile.getLightLevel() > 10;
    }

    @Override
    public int getOptimalHarvestAge(ICropTile iCropTile) {
        return this.getMaxAge() - 2;
    }

    @Override
    public boolean canBeHarvested(ICropTile iCropTile) {
        return iCropTile.getCurrentAge() >= this.getOptimalHarvestAge(iCropTile) && iCropTile.getCurrentAge() < this.getMaxAge();
    }

    @Override
    public ItemStack getGain(ICropTile iCropTile) {
        if (this.canBeHarvested(iCropTile)) {
            return new ItemStack((ItemLike)Blocks.CACTUS);
        }
        return null;
    }

    @Override
    public void tick(ICropTile iCropTile) {
        List<LivingEntity> list;
        if (iCropTile.getCurrentAge() == 0) {
            return;
        }
        BlockPos blockPos = iCropTile.getPosition();
        double d = (double)blockPos.getX() + 0.5;
        double d2 = (double)blockPos.getY() + 0.5;
        double d3 = (double)blockPos.getZ() + 0.5;
        if (iCropTile.getCustomData().getBoolean("eaten")) {
            StackUtil.dropAsEntity(iCropTile.getWorldObj(), blockPos, new ItemStack((ItemLike)Items.ROTTEN_FLESH));
            iCropTile.getCustomData().putBoolean("eaten", false);
        }
        if ((list = iCropTile.getWorldObj().getEntitiesOfClass(LivingEntity.class, new AABB(d - 1.0, (double)blockPos.getY(), d3 - 1.0, d + 1.0, (double)blockPos.getY() + 1.0 + 1.0, d3 + 1.0), null)).isEmpty()) {
            return;
        }
        Collections.shuffle(list);
        for (LivingEntity livingEntity : list) {
            if (livingEntity instanceof Player && ((Player)livingEntity).getAbilities().instabuild) continue;
            livingEntity.setDeltaMovement((d - livingEntity.getX()) * 0.5, Math.min(livingEntity.getDeltaMovement().y(), -0.05), (d3 - livingEntity.getZ()) * 0.5);
            livingEntity.hurt(Ic2DamageSource.cropEating(livingEntity.level()), (float)(iCropTile.getCurrentAge() + 1) * 2.0f);
            if (!CropEating.hasMetalAromor(livingEntity)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 64, 50));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 64, 0));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 64, 0));
            }
            if (this.canGrow(iCropTile)) {
                iCropTile.setGrowthPoints(iCropTile.getGrowthPoints() + 100);
            }
            iCropTile.getWorldObj().playSound(null, d, d2, d3, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1.0f, IC2.random.nextFloat() * 0.1f + 0.9f);
            iCropTile.getCustomData().putBoolean("eaten", true);
            break;
        }
    }

    @Override
    public int getRootsLength(ICropTile iCropTile) {
        return 5;
    }

    @Override
    public int getGrowthDuration(ICropTile iCropTile) {
        float f = 1.0f;
        BlockPos blockPos = iCropTile.getPosition();
        Holder<Biome> holder = BiomeUtil.getBiome((LevelReader)iCropTile.getWorldObj(), blockPos);
        if (IC2.envProxy.biomeHasType(holder, EnvProxy.BiomeType.SWAMP) || IC2.envProxy.biomeHasType(holder, EnvProxy.BiomeType.MOUNTAIN)) {
            f /= 1.5f;
        }
        return (int)((float)super.getGrowthDuration(iCropTile) * (f /= 1.0f + (float)iCropTile.getTerrainAirQuality() / 10.0f));
    }

    private static boolean hasMetalAromor(LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player)) {
            return false;
        }
        Player player = (Player)livingEntity;
        for (ItemStack itemStack : player.getInventory().armor) {
            if (itemStack == null || !ItemWrapper.isMetalArmor(itemStack, player)) continue;
            return true;
        }
        return false;
    }
}

