/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.armor.ItemArmorUtility;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.util.StackUtil;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemArmorNightvisionGoggles
extends ItemArmorUtility
implements IElectricItem,
IItemHudInfo {
    public ItemArmorNightvisionGoggles(Item.Properties properties) {
        super(net.minecraft.core.Holder.direct(Ic2ArmorMaterials.NIGHT_VISION_GOGGLES), ArmorItem.Type.HELMET, properties);
    }

    @Override
    public boolean canProvideEnergy(ItemStack itemStack) {
        return false;
    }

    @Override
    public double getMaxCharge(ItemStack itemStack) {
        return 200000.0;
    }

    @Override
    public int getTier(ItemStack itemStack) {
        return 1;
    }

    @Override
    public double getTransferLimit(ItemStack itemStack) {
        return 200.0;
    }

    @Override
    public List<String> getHudInfo(ItemStack itemStack, boolean bl) {
        LinkedList<String> linkedList = new LinkedList<String>();
        linkedList.add(ElectricItem.manager.getToolTip(itemStack));
        return linkedList;
    }

    // 1.21.1 修复（第二十六轮）：补上电量条。
    //
    // 本类也是 IElectricItem（耗电代替耐久，永不损坏），但它的继承链是
    // ItemArmorNightvisionGoggles → ItemArmorUtility → ItemArmorIC2 → ArmorItem，
    // **没有**经过 BaseElectricItem，也**不是** ItemArmorElectric 的子类，
    // 所以拿不到任何一处的 isBarVisible/getBarWidth/getBarColor 覆写。
    // 1.12.2 里它借用"伪耐久条"（setMaxDamage(27) + 电量映射进 damage）显示电量；
    // 迁移版要去掉 durability(27)（见 Ic2Items 说明），就必须在这里自己提供电量条，
    // 否则护目镜会完全没有电量显示。
    // 实现与 BaseElectricItem / ItemArmorElectric 保持一致。
    public boolean isBarVisible(ItemStack itemStack) {
        return ElectricItem.manager.getChargeLevel(itemStack) < 1.0;
    }

    public int getBarWidth(ItemStack itemStack) {
        return (int)Math.round(ElectricItem.manager.getChargeLevel(itemStack) * 13.0);
    }

    public int getBarColor(ItemStack itemStack) {
        return net.minecraft.util.Mth.hsvToRgb((float)((float)(ElectricItem.manager.getChargeLevel(itemStack) / 3.0)), (float)1.0f, (float)1.0f);
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int n, boolean bl) {
        int n2;
        super.inventoryTick(itemStack, level, entity, n, bl);
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        if (n != this.getEquipmentSlot().getIndex()) {
            return;
        }
        CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
        boolean bl2 = compoundTag.getBoolean("active");
        byte by = compoundTag.getByte("toggleTimer");
        if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && by == 0) {
            by = 10;
            boolean bl3 = bl2 = !bl2;
            if (IC2.sideProxy.isSimulating()) {
                compoundTag.putBoolean("active", bl2);
                if (bl2) {
                    IC2.sideProxy.messagePlayer(player, "Nightvision enabled.", new Object[0]);
                } else {
                    IC2.sideProxy.messagePlayer(player, "Nightvision disabled.", new Object[0]);
                }
            }
        }
        if (IC2.sideProxy.isSimulating() && by > 0) {
            by = (byte)(by - 1);
            compoundTag.putByte("toggleTimer", by);
        }
        if (bl2 && IC2.sideProxy.isSimulating() && (n2 = player.getCommandSenderWorld().getMaxLocalRawBrightness(BlockPos.containing(player.position()))) <= 8 && ElectricItem.manager.use(itemStack, 1.0, (LivingEntity)player)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
            return;
        }
        if (IC2.sideProxy.isSimulating()) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    // TODO: 1.21.1 CreativeModeTab 重构
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        ElectricItemManager.addChargeVariants((Item)this, nonNullList);
    }

    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return false;
    }
}

