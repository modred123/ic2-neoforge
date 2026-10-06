/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package ic2.core.item;

import ic2.core.init.Localization;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.uu.UuIndex;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemCrystalMemory
extends Item {
    public static final String TOOLTIP_ITEM = "item.ic2.crystal_memory.tooltip.item";
    public static final String TOOLTIP_UU_MATTER = "item.ic2.crystal_memory.tooltip.uu_matter";
    public static final String TOOLTIP_ENERGY = "item.ic2.crystal_memory.tooltip.energy";
    public static final String TOOLTIP_EMPTY = "item.ic2.crystal_memory.tooltip.empty";

    public ItemCrystalMemory(Item.Properties properties) {
        super(properties);
    }

    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        ItemStack itemStack2 = this.readItemStack(itemStack);
        if (!StackUtil.isEmpty(itemStack2)) {
            list.add((Component)Component.literal((String)(Localization.translate(TOOLTIP_ITEM) + " " + itemStack2.getHoverName())).withStyle(ChatFormatting.GRAY));
            list.add((Component)Component.literal((String)(Localization.translate(TOOLTIP_UU_MATTER) + " " + Util.toSiString(UuIndex.instance.getInBuckets(itemStack2), 4) + "B")).withStyle(ChatFormatting.GRAY));
        } else {
            list.add((Component)Component.translatable((String)TOOLTIP_EMPTY).withStyle(ChatFormatting.GRAY));
        }
    }

    public ItemStack readItemStack(ItemStack itemStack) {
        CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
        CompoundTag compoundTag2 = compoundTag.getCompound("Pattern");
        ItemStack itemStack2 = ItemStack.parseOptional(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), compoundTag2);
        return itemStack2;
    }

    public void writecontentsTag(ItemStack itemStack, ItemStack itemStack2) {
        CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
        CompoundTag compoundTag2 = new CompoundTag();
        itemStack2.save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), compoundTag2);
        compoundTag.put("Pattern", (Tag)compoundTag2);
    }
}

