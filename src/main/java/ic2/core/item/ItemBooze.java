/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.ItemMeshDefinition
 *  net.minecraft.client.renderer.block.model.ModelBakery
 *  net.minecraft.client.renderer.block.model.ModelResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.init.MobEffects
 *  net.minecraft.item.UseAnim
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.model.ModelLoader
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.block.state.IIdProvider;
import ic2.core.item.ItemIC2;
import ic2.core.item.ItemMug;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;


import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;

public class ItemBooze
extends ItemIC2 {
    public String[] solidRatio = new String[]{"Watery ", "Clear ", "Lite ", "", "Strong ", "Thick ", "Stodge ", "X"};
    public String[] hopsRatio = new String[]{"Soup ", "Alcfree ", "White ", "", "Dark ", "Full ", "Black ", "X"};
    public String[] timeRatioNames = new String[]{"Brew", "Youngster", "Beer", "Ale", "Dragonblood", "Black Stuff"};
    public int[] baseDuration = new int[]{300, 600, 900, 1200, 1600, 2000, 2400};
    public float[] baseIntensity = new float[]{0.4f, 0.75f, 1.0f, 1.5f, 2.0f};
    public static float rumStackability = 2.0f;
    public static int rumDuration = 600;

    public ItemBooze() {
        super(ItemName.booze_mug);
        this.setMaxStackSize(1);
        this.setCreativeTab(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(final ItemName name) {
        
        for (BoozeMugType type : BoozeMugType.values) {
            
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemstack) {
        int meta = itemstack.getDamageValue();
        int type = ItemBooze.getTypeOfValue(meta);
        if (type == 1) {
            int timeRatio = Math.min(ItemBooze.getTimeRatioOfBeerValue(meta), this.timeRatioNames.length - 1);
            if (timeRatio == this.timeRatioNames.length - 1) {
                return this.timeRatioNames[timeRatio];
            }
            return this.solidRatio[ItemBooze.getSolidRatioOfBeerValue(meta)] + this.hopsRatio[ItemBooze.getHopsRatioOfBeerValue(meta)] + this.timeRatioNames[timeRatio];
        }
        if (type == 2) {
            return "Rum";
        }
        return "Zero";
    }

    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {
        int meta = stack.getDamageValue();
        int type = ItemBooze.getTypeOfValue(meta);
        if (type == 0) {
            return ItemName.mug.getItemStack(ItemMug.MugType.empty);
        }
        if (type == 1) {
            if (ItemBooze.getTimeRatioOfBeerValue(meta) == 5) {
                return this.drinkBlackStuff(living);
            }
            int solidRatio = ItemBooze.getSolidRatioOfBeerValue(meta);
            int alc = ItemBooze.getHopsRatioOfBeerValue(meta);
            int duration = this.baseDuration[solidRatio];
            float intensity = this.baseIntensity[ItemBooze.getTimeRatioOfBeerValue(meta)];
            if (living instanceof Player) {
                ((Player)living).getFoodData().eat(6 - alc, (float)solidRatio * 0.15f);
            }
            int max = (int)(intensity * ((float)alc * 0.5f));
            MobEffectInstance slow = living.getEffect(MobEffects.DIG_SLOWDOWN);
            int level = -1;
            if (slow != null) {
                level = slow.getAmplifier();
            }
            this.amplifyEffect(living, MobEffects.DIG_SLOWDOWN, max, intensity, duration);
            if (level > -1) {
                this.amplifyEffect(living, MobEffects.DAMAGE_BOOST, max, intensity, duration);
                if (level > 0) {
                    this.amplifyEffect(living, MobEffects.MOVEMENT_SLOWDOWN, max / 2, intensity, duration);
                    if (level > 1) {
                        this.amplifyEffect(living, MobEffects.DAMAGE_RESISTANCE, max - 1, intensity, duration);
                        if (level > 2) {
                            this.amplifyEffect(living, MobEffects.CONFUSION, 0, intensity, duration);
                            if (level > 3) {
                                living.addEffect(new MobEffectInstance(MobEffects.HARM, 1, living.level().getRandom().nextInt(3)));
                            }
                        }
                    }
                }
            }
        }
        if (type == 2) {
            if (ItemBooze.getProgressOfRumValue(meta) < 100) {
                this.drinkBlackStuff(living);
            } else {
                this.amplifyEffect(living, MobEffects.FIRE_RESISTANCE, 0, rumStackability, rumDuration);
                MobEffectInstance def = living.getEffect(MobEffects.DAMAGE_RESISTANCE);
                int level = -1;
                if (def != null) {
                    level = def.getAmplifier();
                }
                this.amplifyEffect(living, MobEffects.DAMAGE_RESISTANCE, 2, rumStackability, rumDuration);
                if (level >= 0) {
                    this.amplifyEffect(living, MobEffects.BLINDNESS, 0, rumStackability, rumDuration);
                }
                if (level >= 1) {
                    this.amplifyEffect(living, MobEffects.CONFUSION, 0, rumStackability, rumDuration);
                }
            }
        }
        return ItemName.mug.getItemStack(ItemMug.MugType.empty);
    }

    public void amplifyEffect(LivingEntity living, net.minecraft.core.Holder<MobEffect> potion, int max, float intensity, int duration) {
        MobEffectInstance eff = living.getEffect(potion);
        if (eff == null) {
            living.addEffect(new MobEffectInstance(potion, duration, 0));
        } else {
            int currentDuration = eff.getDuration();
            int maxnewdur = (int)((float)duration * (1.0f + intensity * 2.0f) - (float)currentDuration) / 2;
            if (maxnewdur < 0) {
                maxnewdur = 0;
            }
            if (maxnewdur < duration) {
                duration = maxnewdur;
            }
            currentDuration += duration;
            int newamp = eff.getAmplifier();
            if (newamp < max) {
                ++newamp;
            }
            living.addEffect(new MobEffectInstance(potion, currentDuration, newamp));
        }
    }

    public ItemStack drinkBlackStuff(LivingEntity living) {
        switch (living.level().getRandom().nextInt(6)) {
            case 1: {
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1200, 0));
                break;
            }
            case 2: {
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 2400, 0));
                break;
            }
            case 3: {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 2400, 0));
                break;
            }
            case 4: {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
                break;
            }
            case 5: {
                living.addEffect(new MobEffectInstance(MobEffects.HARM, 1, living.level().getRandom().nextInt(4)));
            }
        }
        return ItemName.mug.getItemStack(ItemMug.MugType.empty);
    }

    public int getUseDuration(ItemStack itemstack, LivingEntity living) {
        return 32;
    }

    public UseAnim getUseAnimation(ItemStack itemstack) {
        return UseAnim.DRINK;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public static int getTypeOfValue(int value) {
        return ItemBooze.unpackValue(value, 0, 2);
    }

    public static int getAmountOfValue(int value) {
        if (ItemBooze.getTypeOfValue(value) == 0) {
            return 0;
        }
        return ItemBooze.unpackValue(value, 2, 5) + 1;
    }

    public static int getSolidRatioOfBeerValue(int value) {
        return ItemBooze.unpackValue(value, 7, 3);
    }

    public static int getHopsRatioOfBeerValue(int value) {
        return ItemBooze.unpackValue(value, 10, 3);
    }

    public static int getTimeRatioOfBeerValue(int value) {
        return ItemBooze.unpackValue(value, 13, 3);
    }

    public static int getProgressOfRumValue(int value) {
        return ItemBooze.unpackValue(value, 7, 7);
    }

    private static int unpackValue(int value, int bitshift, int take) {
        int mask = (1 << take) - 1;
        return (value >>>= bitshift) & mask;
    }

    private static enum BoozeMugType implements IIdProvider
    {
        beer_brew,
        beer_youngster,
        beer_beer,
        beer_ale,
        beer_dragon_blood,
        beer_black_stuff,
        rum;

        public static final BoozeMugType[] values;

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public int getId() {
            throw new UnsupportedOperationException();
        }

        static {
            values = BoozeMugType.values();
        }
    }
}

