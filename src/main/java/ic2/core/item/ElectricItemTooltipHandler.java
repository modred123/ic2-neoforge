/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item;

import ic2.api.item.ElectricItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ElectricItemTooltipHandler {
    public static void addTooltip(ItemStack itemStack, List<Component> list) {
        String string;
        if (itemStack != null && ElectricItem.manager.getMaxCharge(itemStack) > 0.0 && (string = ElectricItem.manager.getToolTip(itemStack)) != null && !string.trim().isEmpty()) {
            list.add((Component)Component.literal((String)string));
            // 第四十轮：能量等级改为**始终显示**（1.12.2 需按 Shift，但用户需求是"接线前一眼看到耐压"，
            // 以便判断会不会过压爆炸）—— `ic2.item.tooltip.PowerTier` = "能量等级: %1$s"。
            list.add((Component)Component.translatable((String)"ic2.item.tooltip.PowerTier", (Object[])new Object[]{ElectricItem.manager.getTier(itemStack)}).withStyle(ChatFormatting.GRAY));
        }
    }
}

