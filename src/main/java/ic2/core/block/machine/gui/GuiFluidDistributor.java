/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core.block.machine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiFluidDistributor
extends Ic2Gui<ContainerFluidDistributor> {
    public GuiFluidDistributor(ContainerFluidDistributor containerFluidDistributor, Inventory inventory, Component component) {
        super(containerFluidDistributor, inventory, component, 184);
        this.addElement(TankGauge.createPlain(this, 29, 38, 55, 47, ((TileEntityFluidDistributor)containerFluidDistributor.base).fluidTank));
    }

    @Override
    protected void drawForegroundLayer(PoseStack poseStack, int n, int n2) {
        super.drawForegroundLayer(poseStack, n, n2);
        this.drawString(poseStack, 112, 56, Localization.translate("ic2.FluidDistributor.gui.mode.info"), 5752026);
        if (((TileEntityFluidDistributor)((ContainerFluidDistributor)this.menu).base).getActive()) {
            this.drawString(poseStack, 95, 80, Localization.translate("ic2.FluidDistributor.gui.mode.concentrate"), 5752026);
        } else {
            this.drawString(poseStack, 95, 80, Localization.translate("ic2.FluidDistributor.gui.mode.distribute"), 5752026);
        }
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        d -= (double)this.leftPos;
        d2 -= (double)this.topPos;
        if (d >= 117.0 && d2 >= 58.0 && d <= 135.0 && d2 <= 66.0) {
            IC2.network.get(false).initiateClientTileEntityEvent((BlockEntity)((ContainerFluidDistributor)this.menu).base, 1);
            return true;
        }
        return super.mouseClicked(d += (double)this.leftPos, d2 += (double)this.topPos, n);
    }

    @Override
    protected ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guifluiddistributor.png");
    }
}

