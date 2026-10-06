/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package ic2.core.item.resources;

import ic2.api.item.IKineticRotor;
import ic2.core.block.kineticgenerator.gui.GuiWaterKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiWindKineticGenerator;
import ic2.core.profile.NotClassic;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemWindRotor
extends Item
implements IKineticRotor {
    private final int maxWindStrength;
    private final int minWindStrength;
    private final int radius;
    private final float efficiency;
    private final ResourceLocation renderTexture;
    private final boolean water;

    public ItemWindRotor(Item.Properties properties, int n, boolean bl, float f, int n2, int n3, ResourceLocation resourceLocation) {
        super(properties);
        this.radius = n;
        this.efficiency = f;
        this.renderTexture = resourceLocation;
        this.minWindStrength = n2;
        this.maxWindStrength = n3;
        this.water = bl;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        list.add((Component)Component.translatable((String)"ic2.itemrotor.wind.info", (Object[])new Object[]{this.minWindStrength, this.maxWindStrength}).withStyle(ChatFormatting.GRAY));
        IKineticRotor.GearboxType gearboxType = null;
        if (Minecraft.getInstance().screen instanceof GuiWaterKineticGenerator) {
            gearboxType = IKineticRotor.GearboxType.WATER;
        } else if (Minecraft.getInstance().screen instanceof GuiWindKineticGenerator) {
            gearboxType = IKineticRotor.GearboxType.WIND;
        }
        if (gearboxType != null) {
            list.add((Component)Component.translatable((String)("ic2.itemrotor.fitsin." + this.isAcceptedType(itemStack, gearboxType))).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public int getDiameter(ItemStack itemStack) {
        return this.radius;
    }

    @Override
    public ResourceLocation getRotorRenderTexture(ItemStack itemStack) {
        return this.renderTexture;
    }

    @Override
    public float getEfficiency(ItemStack itemStack) {
        return this.efficiency;
    }

    @Override
    public int getMinWindStrength(ItemStack itemStack) {
        return this.minWindStrength;
    }

    @Override
    public int getMaxWindStrength(ItemStack itemStack) {
        return this.maxWindStrength;
    }

    @Override
    public boolean isAcceptedType(ItemStack itemStack, IKineticRotor.GearboxType gearboxType) {
        return gearboxType == IKineticRotor.GearboxType.WIND || this.water;
    }
}

