/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package ic2.core.item;

import ic2.core.Ic2Potion;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemTerraWart
extends Item {
    public ItemTerraWart(Item.Properties properties) {
        super(properties);
    }

    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        livingEntity.removeEffect(MobEffects.CONFUSION);
        livingEntity.removeEffect(MobEffects.DIG_SLOWDOWN);
        livingEntity.removeEffect(MobEffects.HUNGER);
        livingEntity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        livingEntity.removeEffect(MobEffects.WEAKNESS);
        livingEntity.removeEffect(MobEffects.BLINDNESS);
        livingEntity.removeEffect(MobEffects.POISON);
        livingEntity.removeEffect(MobEffects.WITHER);
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(Ic2Potion.radiation.getHolder());
        if (mobEffectInstance != null) {
            if (mobEffectInstance.getDuration() <= 600) {
                livingEntity.removeEffect(Ic2Potion.radiation.getHolder());
            } else {
                livingEntity.removeEffect(Ic2Potion.radiation.getHolder());
                Ic2Potion.radiation.applyTo(livingEntity, mobEffectInstance.getDuration() - 600, mobEffectInstance.getAmplifier());
            }
        }
        return super.finishUsingItem(itemStack, level, livingEntity);
    }
}

