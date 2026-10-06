/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package ic2.core.item;

import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class ItemMug
extends Item
implements ItemLike {
    public MugType mugType;

    public ItemMug(Item.Properties properties, MugType mugType) {
        super(properties);
        this.mugType = mugType;
    }

    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        int n;
        if (!(livingEntity instanceof Player)) {
            return itemStack;
        }
        Player player = (Player)livingEntity;
        MugType mugType = this.getType(itemStack);
        if (mugType == null || mugType == MugType.empty) {
            return itemStack;
        }
        int n2 = switch (mugType) {
            case MugType.cold_coffee -> {
                n = 1;
                yield 600;
            }
            case MugType.dark_coffee -> {
                n = 5;
                yield 1200;
            }
            case MugType.coffee -> {
                n = 6;
                yield 1200;
            }
            default -> throw new IllegalStateException("unexpected type: " + mugType);
        };
        int n3 = 0;
        int n4 = this.amplifyEffect(player, MobEffects.MOVEMENT_SPEED, n, n2);
        if (n4 > n3) {
            n3 = n4;
        }
        if ((n4 = this.amplifyEffect(player, MobEffects.DIG_SPEED, n, n2)) > n3) {
            n3 = n4;
        }
        if (mugType == MugType.coffee) {
            n3 -= 2;
        }
        if (n3 >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (n3 - 2) * 200, 0));
            if (n3 >= 4) {
                player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, n3 - 3));
            }
        }
        return new ItemStack((ItemLike)Ic2Items.EMPTY_MUG, 1);
    }

    private int amplifyEffect(Player player, net.minecraft.core.Holder<MobEffect> mobEffect, int n, int n2) {
        MobEffectInstance mobEffectInstance = player.getEffect(mobEffect);
        if (mobEffectInstance != null) {
            int n3 = mobEffectInstance.getAmplifier();
            int n4 = mobEffectInstance.getDuration();
            if (n3 < n) {
                ++n3;
            }
            n4 += n2;
            assert (mobEffect.equals(mobEffectInstance.getEffect()));
            player.addEffect(new MobEffectInstance(mobEffect, n4, n3));
            return n3;
        }
        player.addEffect(new MobEffectInstance(mobEffect, 300, 0));
        return 1;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, net.minecraft.world.entity.LivingEntity livingEntity) {
        MugType mugType = this.getType(itemStack);
        if (mugType == null || mugType == MugType.empty) {
            return 0;
        }
        return 32;
    }

    public UseAnim getUseAnimation(ItemStack itemStack) {
        MugType mugType = this.getType(itemStack);
        if (mugType == null || mugType == MugType.empty) {
            return UseAnim.NONE;
        }
        return UseAnim.DRINK;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        MugType mugType = this.getType(StackUtil.get(player, interactionHand));
        if (mugType != null && mugType != MugType.empty) {
            player.startUsingItem(interactionHand);
        }
        return super.use(level, player, interactionHand);
    }

    private MugType getType(ItemStack itemStack) {
        return ((ItemMug)itemStack.getItem()).mugType;
    }

    public static enum MugType {
        empty,
        cold_coffee,
        dark_coffee,
        coffee;

    }
}

