/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  net.minecraft.core.NonNullList
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ArmorMaterial
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package ic2.core.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.ElectricItemTooltipHandler;
import ic2.core.item.armor.ItemArmorIC2;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class ItemArmorElectric
extends ItemArmorIC2
implements IElectricItem {
    public static final ResourceLocation[] MODIFIERS = new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath("ic2", "armor_modifier_feet"), ResourceLocation.fromNamespaceAndPath("ic2", "armor_modifier_legs"), ResourceLocation.fromNamespaceAndPath("ic2", "armor_modifier_chest"), ResourceLocation.fromNamespaceAndPath("ic2", "armor_modifier_head")};
    protected final double maxCharge;
    protected final double transferLimit;
    protected final int tier;

    public ItemArmorElectric(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Item.Properties properties, double d, double d2, int n) {
        super(armorMaterial, type, properties);
        this.maxCharge = d;
        this.transferLimit = d2;
        this.tier = n;
    }

    /** ex112 兼容构造：ItemName + 字符串类型 + EquipmentSlot（映射到 Ic2ArmorMaterials + 注册） */
    protected ItemArmorElectric(ic2.core.ref.ItemName name, String materialName, net.minecraft.world.entity.EquipmentSlot slot, double d, double d2, int n) {
        this(ItemArmorElectric.resolveMaterial(name, materialName), ItemArmorElectric.resolveType(slot), new Item.Properties(), d, d2, n);
        ic2.core.init.BlocksItems.registerItem(this, ic2.core.IC2.getIdentifier(name.name()));
        name.setInstance(this);
    }

    protected static Holder<ArmorMaterial> resolveMaterial(ic2.core.ref.ItemName name, String materialName) {
        if (name == ic2.core.ref.ItemName.jetpack_electric || name == ic2.core.ref.ItemName.jetpack) {
            return Holder.direct(ic2.core.ref.Ic2ArmorMaterials.JET_PACK);
        }
        if (name == ic2.core.ref.ItemName.cf_pack || name == ic2.core.ref.ItemName.cf_pack) {
            return Holder.direct(ic2.core.ref.Ic2ArmorMaterials.CF_PACK);
        }
        if (name == ic2.core.ref.ItemName.nano_chestplate || name == ic2.core.ref.ItemName.nano_boots) {
            return Holder.direct(ic2.core.ref.Ic2ArmorMaterials.NANO_SUIT);
        }
        if (name == ic2.core.ref.ItemName.quantum_chestplate || name == ic2.core.ref.ItemName.quantum_boots) {
            return Holder.direct(ic2.core.ref.Ic2ArmorMaterials.QUANTUM_SUIT);
        }
        return Holder.direct(ic2.core.ref.Ic2ArmorMaterials.BRONZE);
    }

    protected static ArmorItem.Type resolveType(net.minecraft.world.entity.EquipmentSlot slot) {
        if (slot == net.minecraft.world.entity.EquipmentSlot.HEAD) return ArmorItem.Type.HELMET;
        if (slot == net.minecraft.world.entity.EquipmentSlot.CHEST) return ArmorItem.Type.CHESTPLATE;
        if (slot == net.minecraft.world.entity.EquipmentSlot.LEGS) return ArmorItem.Type.LEGGINGS;
        return ArmorItem.Type.BOOTS;
    }

    public abstract int getEnergyPerDamage();

    public static void damageArmor(Player player, DamageSource damageSource, float f) {
        if (f <= 0.0f || damageSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        float f2 = f / 4.0f;
        if (f2 < 1.0f) {
            f2 = 1.0f;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = player.getItemBySlot(equipmentSlot);
            Item item = itemStack.getItem();
            if (!(item instanceof ItemArmorElectric)) continue;
            ItemArmorElectric itemArmorElectric = (ItemArmorElectric)item;
            itemArmorElectric.damageArmor((LivingEntity)player, itemStack, damageSource, f2, equipmentSlot);
        }
    }

    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

    // TODO: 1.21.1 CreativeModeTab 重构，fillItemCategory 已移除，充电变体需在 Tab displayItems 中处理
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        ElectricItemManager.addChargeVariants((Item)this, nonNullList);
    }

    // 1.21.1 修复（第二十四轮）：此处的 appendHoverText 原本只做一件事——
    // 调用 ElectricItemTooltipHandler.addTooltip。该逻辑已上移到全局事件入口
    // EventHandlerClient.onDrawTooltip（对齐 1.12.2 的事件驱动设计），
    // 若保留此处会导致电量行在装甲上**重复显示两次**，故移除。

    public void damageArmor(LivingEntity livingEntity, ItemStack itemStack, DamageSource damageSource, double d, EquipmentSlot equipmentSlot) {
        ElectricItem.manager.discharge(itemStack, d * (double)this.getEnergyPerDamage(), Integer.MAX_VALUE, true, false, false);
    }

    @Override
    public boolean canProvideEnergy(ItemStack itemStack) {
        return false;
    }

    @Override
    public double getMaxCharge(ItemStack itemStack) {
        return this.maxCharge;
    }

    @Override
    public int getTier(ItemStack itemStack) {
        return this.tier;
    }

    @Override
    public double getTransferLimit(ItemStack itemStack) {
        return this.transferLimit;
    }

    public boolean isBarVisible(ItemStack itemStack) {
        return ElectricItem.manager.getChargeLevel(itemStack) < 1.0;
    }

    public int getBarWidth(ItemStack itemStack) {
        return (int)Math.round(ElectricItem.manager.getChargeLevel(itemStack) * 13.0);
    }

    public int getBarColor(ItemStack itemStack) {
        return Mth.hsvToRgb((float)((float)(ElectricItem.manager.getChargeLevel(itemStack) / 3.0)), (float)1.0f, (float)1.0f);
    }

    public ItemAttributeModifiers getAttributeModifiers(ItemStack itemStack, EquipmentSlot equipmentSlot) {
        int n;
        if (equipmentSlot != this.getEquipmentSlot()) {
            return this.getDefaultAttributeModifiers();
        }
        boolean bl = ElectricItem.manager.getCharge(itemStack) >= (double)((ItemArmorElectric)itemStack.getItem()).getEnergyPerDamage();
        if (!bl) {
            return this.getDefaultAttributeModifiers();
        }
        Item item = itemStack.getItem();
        if (item instanceof ItemArmorNanoSuit) {
            n = ItemArmorNanoSuit.CHARGED_PROTECTION[equipmentSlot.getIndex()];
        } else if (item instanceof ItemArmorQuantumSuit) {
            n = ItemArmorQuantumSuit.CHARGED_PROTECTION[equipmentSlot.getIndex()];
        } else {
            return this.getDefaultAttributeModifiers();
        }
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        Holder<Attribute> attribute = Attributes.ARMOR;
        ResourceLocation modifierId = MODIFIERS[equipmentSlot.getIndex()];
        for (ItemAttributeModifiers.Entry entry : this.getDefaultAttributeModifiers().modifiers()) {
            if (entry.modifier().id().equals(modifierId)) continue;
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        builder.add(attribute, new AttributeModifier(modifierId, (double)n, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(equipmentSlot));
        return builder.build();
    }
}

