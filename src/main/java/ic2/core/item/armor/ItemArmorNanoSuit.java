/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IItemHudProvider;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.armor.ItemArmorElectric;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ItemArmorNanoSuit
extends ItemArmorElectric
implements IItemHudProvider {
    public static final int[] CHARGED_PROTECTION = new int[]{3, 6, 8, 3};

    public ItemArmorNanoSuit(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Item.Properties properties) {
        super(armorMaterial, type, properties, 1000000.0, 1600.0, 3);
    }

    @Override
    public int getEnergyPerDamage() {
        return 5000;
    }

    public boolean absorbFall(ItemStack itemStack, LivingEntity livingEntity, float f) {
        int n = Math.max((int)f - 3, 0);
        if (n >= 8) {
            return false;
        }
        double d = this.getEnergyPerDamage() * n;
        if (d > ElectricItem.manager.getCharge(itemStack)) {
            return false;
        }
        ElectricItem.manager.discharge(itemStack, d, Integer.MAX_VALUE, true, false, false);
        return true;
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int n, boolean bl) {
        super.inventoryTick(itemStack, level, entity, n, bl);
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
        byte by = compoundTag.getByte("toggleTimer");
        boolean bl2 = false;
        if (n == EquipmentSlot.HEAD.getIndex()) {
            int n2;
            boolean bl3 = compoundTag.getBoolean("Nightvision");
            short s = compoundTag.getShort("HudMode");
            if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && by == 0) {
                by = 10;
                boolean bl4 = bl3 = !bl3;
                if (IC2.sideProxy.isSimulating()) {
                    compoundTag.putBoolean("Nightvision", bl3);
                    if (bl3) {
                        IC2.sideProxy.messagePlayer(player, "Nightvision enabled.", new Object[0]);
                    } else {
                        IC2.sideProxy.messagePlayer(player, "Nightvision disabled.", new Object[0]);
                    }
                }
            }
            if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isHudModeKeyDown(player) && by == 0) {
                by = 10;
                s = s == HudMode.getMaxMode() ? (short)0 : (short)(s + 1);
                if (IC2.sideProxy.isSimulating()) {
                    compoundTag.putShort("HudMode", s);
                    IC2.sideProxy.messagePlayer(player, Localization.translate(HudMode.getFromID(s).getTranslationKey()), new Object[0]);
                }
            }
            if (IC2.sideProxy.isSimulating() && by > 0) {
                by = (byte)(by - 1);
                compoundTag.putByte("toggleTimer", by);
            }
            if (bl3 && IC2.sideProxy.isSimulating() && (n2 = player.getCommandSenderWorld().getMaxLocalRawBrightness(BlockPos.containing(player.position()))) <= 8 && ElectricItem.manager.use(itemStack, 1.0, (LivingEntity)player)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
                return;
            }
            if (IC2.sideProxy.isSimulating()) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    public Rarity getRarity(ItemStack itemStack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public boolean doesProvideHUD(ItemStack itemStack) {
        return this.getEquipmentSlot() == EquipmentSlot.HEAD && ElectricItem.manager.getCharge(itemStack) > 0.0;
    }

    @Override
    public HudMode getHudMode(ItemStack itemStack) {
        return HudMode.getFromID(StackUtil.getOrCreateNbtData(itemStack).getShort("HudMode"));
    }
}

