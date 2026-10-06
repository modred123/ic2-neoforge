/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.item.reactor.AbstractDamageableReactorComponent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemReactorHeatStorage
extends AbstractDamageableReactorComponent {
    public ItemReactorHeatStorage(Item.Properties properties, int n) {
        super(properties, n);
    }

    @Override
    public boolean canStoreHeat(ItemStack itemStack, IReactor iReactor, int n, int n2) {
        return true;
    }

    @Override
    public int getMaxHeat(ItemStack itemStack, IReactor iReactor, int n, int n2) {
        return this.getMaxUse();
    }

    @Override
    public int getCurrentHeat(ItemStack itemStack, IReactor iReactor, int n, int n2) {
        return this.getUse(itemStack);
    }

    @Override
    public int alterHeat(ItemStack itemStack, IReactor iReactor, int n, int n2, int n3) {
        int n4 = this.getCurrentHeat(itemStack, iReactor, n, n2);
        int n5 = this.getMaxHeat(itemStack, iReactor, n, n2);
        if ((n4 += n3) > n5) {
            iReactor.setItemAt(n, n2, null);
            n3 = n5 - n4 + 1;
        } else {
            if (n4 < 0) {
                n3 = n4;
                n4 = 0;
            } else {
                n3 = 0;
            }
            this.setUse(itemStack, n4);
        }
        return n3;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        if (this.getUse(itemStack) > 0) {
            list.add((Component)Component.literal((String)"ic2.reactoritem.heatwarning.line1").withStyle(ChatFormatting.GRAY));
            list.add((Component)Component.literal((String)"ic2.reactoritem.heatwarning.line2").withStyle(ChatFormatting.GRAY));
        }
    }
}

