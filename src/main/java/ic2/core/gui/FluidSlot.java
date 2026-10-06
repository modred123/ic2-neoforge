/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraftforge.fluids.Fluid
 *  net.minecraftforge.fluids.FluidStack
 *  net.minecraftforge.fluids.IFluidTank
 */
package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.GuiIC2;
import ic2.core.fluid.FluidHandler;
import ic2.core.gui.GuiElement;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

public class FluidSlot
extends GuiElement<FluidSlot> {
    public static final int posU = 8;
    public static final int posV = 160;
    public static final int normalWidth = 18;
    public static final int normalHeight = 18;
    public static final int fluidOffsetX = 1;
    public static final int fluidOffsetY = 1;
    public static final int fluidNetWidth = 16;
    public static final int fluidNetHeight = 16;
    private final IFluidTank tank;

    public static FluidSlot createFluidSlot(GuiIC2<?> gui, int x, int y, IFluidTank tank) {
        return new FluidSlot(gui, x, y, 18, 18, tank);
    }

    protected FluidSlot(GuiIC2<?> gui, int x, int y, int width, int height, IFluidTank tank) {
        super(gui, x, y, width, height);
        if (tank == null) {
            throw new NullPointerException("Null FluidTank instance.");
        }
        this.tank = tank;
    }

    @Override
    public void drawBackground(PoseStack poseStack, int mouseX, int mouseY) {
        FluidSlot.bindCommonTexture();
        FluidStack fs = this.tank.getFluid();
        this.gui.drawTexturedRect(poseStack, this.x, this.y, this.width, this.height, 8.0, 160.0);
        if (fs != null && fs.getAmount() > 0) {
            int fluidX = this.x + 1;
            int fluidY = this.y + 1;
            int fluidWidth = 16;
            int fluidHeight = 16;
            Fluid fluid = fs.getFluid();
            TextureAtlasSprite sprite = fluid != null ? FluidSlot.getBlockTextureMap().getSprite(FluidHandler.getStillSpriteId(fluid)) : null;
            int color = fluid != null ? FluidHandler.getColor(fluid) : -1;
            FluidSlot.bindBlockTexture();
            this.gui.drawSprite(poseStack, fluidX, fluidY, fluidWidth, fluidHeight, sprite, color, 1.0, false, false);
        }
    }

    @Override
    protected List<Component> getToolTip() {
        List<Component> ret = super.getToolTip();
        FluidStack fs = this.tank.getFluid();
        if (fs == null || fs.getAmount() <= 0) {
            ret.add(Component.literal("No Fluid"));
            ret.add(Component.literal("Amount: 0 " + Localization.translate("ic2.generic.text.mb")));
            ret.add(Component.literal("Type: Not Available"));
        } else {
            Fluid fluid = fs.getFluid();
            if (fluid != null) {
                ret.add(Component.translatable(Util.getName(fluid).toString()));
                ret.add(Component.literal("Amount: " + fs.getAmount() + " " + Localization.translate("ic2.generic.text.mb")));
                String state = FluidHandler.isGaseous(fluid) ? "Gas" : "Liquid";
                ret.add(Component.literal("Type: " + state));
            } else {
                ret.add(Component.literal("Invalid FluidStack instance."));
            }
        }
        return ret;
    }
}

