/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 */
package ic2.core.block.machine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSolarDestiller
extends Ic2Gui<ContainerSolarDestiller> {
    public GuiSolarDestiller(ContainerSolarDestiller containerSolarDestiller, Inventory inventory, Component component) {
        super(containerSolarDestiller, inventory, component, 184);
        this.addElement(TankGauge.createPlain(this, 37, 43, 53, 18, ((TileEntitySolarDestiller)containerSolarDestiller.base).inputTank));
        this.addElement(TankGauge.createPlain(this, 115, 55, 17, 43, ((TileEntitySolarDestiller)containerSolarDestiller.base).outputTank));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        PoseStack poseStack = guiGraphics.pose();
        super.renderBg(guiGraphics, f, n, n2);
        this.bindTexture();
        if (((TileEntitySolarDestiller)((ContainerSolarDestiller)this.menu).base).canWork()) {
            // 第三十四轮：drawTexturedRect 内部已按阶段补 (leftPos, topPos)，此处不能再补一次（否则偏移一倍）。
            this.drawTexturedRect(poseStack, 36, 26, 0.0, 184.0, 97.0, 29.0);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guisolardestiller.png");
    }
}

