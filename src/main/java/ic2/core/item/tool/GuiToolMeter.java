/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.tool;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.GuiIC2;
import ic2.core.gui.CustomButton;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.MouseButton;
import ic2.core.init.Localization;
import ic2.core.item.tool.ContainerMeter;
import ic2.core.util.Util;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class GuiToolMeter
extends GuiIC2<ContainerMeter> {
    public GuiToolMeter(ContainerMeter container) {
        super(container, 217);
        this.addElement((GuiElement<?>)new CustomButton(this, 112, 55, 20, 20, this.createModeSetter(ContainerMeter.Mode.EnergyIn)).withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.EnergyIn"));
        this.addElement((GuiElement<?>)new CustomButton(this, 132, 55, 20, 20, this.createModeSetter(ContainerMeter.Mode.EnergyOut)).withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.EnergyOut"));
        this.addElement((GuiElement<?>)new CustomButton(this, 112, 75, 20, 20, this.createModeSetter(ContainerMeter.Mode.EnergyGain)).withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.EnergyGain"));
        this.addElement((GuiElement<?>)new CustomButton(this, 132, 75, 20, 20, this.createModeSetter(ContainerMeter.Mode.Voltage)).withTooltip("ic2.itemToolMEter.mode.switch\nic2.itemToolMEter.mode.Voltage"));
    }

    /**
     * 第三十三轮：1.21.1 的 `ClientEnvProxy.ScreenFactory` 是 (menu, inventory, component) 三参函数式接口，
     * 界面类必须提供三参构造器。本 GUI 的尺寸与标题都由自身贴图/元素决定，
     * 因此 inventory / component 仅用于满足签名（与 1.12.2 的 GuiToolMeter 行为一致）。
     */
    public GuiToolMeter(ContainerMeter containerMeter, Inventory inventory, Component component) {
        this(containerMeter);
    }

    private IClickHandler createModeSetter(final ContainerMeter.Mode mode) {
        return new IClickHandler(){

            @Override
            public void onClick(MouseButton button) {
                ((ContainerMeter)GuiToolMeter.this.container).setMode(mode);
            }
        };
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        int xMin = (this.width - this.imageWidth) / 2;
        int yMin = (this.height - this.imageHeight) / 2;
        int x = i - xMin;
        int y = j - yMin;
        if (x >= 26 && y >= 111 && x <= 83 && y <= 123) {
            // 第四十三轮：1.12.2 原版的"重置"是手写矩形判定（不是按钮），点下去**没有任何音效**，
            // 玩家无法判断有没有点中（用户实测反馈"点击重置没有听到声音反馈"）。
            // 这里补一个原版风格的按钮音效，坐标判定与重置行为完全不变。
            net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0f));
            ((ContainerMeter)this.container).reset();
        }
    }

    @Override
    protected void drawForegroundLayer(int mouseX, int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        String unit = ((ContainerMeter)this.container).getMode() == ContainerMeter.Mode.Voltage ? "ic2.generic.text.v" : "ic2.generic.text.EUt";
        unit = Localization.translate(unit);
        this.drawString(115, 43, Localization.translate("ic2.itemToolMEter.mode"), 2157374, false);
        this.drawString(15, 41, Localization.translate("ic2.itemToolMEter.avg"), 2157374, false);
        this.drawString(15, 51, "" + Util.toSiString(((ContainerMeter)this.container).getResultAvg(), 6) + unit, 2157374, false);
        this.drawString(15, 64, Localization.translate("ic2.itemToolMEter.max/min"), 2157374, false);
        this.drawString(15, 74, "" + Util.toSiString(((ContainerMeter)this.container).getResultMax(), 6) + unit, 2157374, false);
        this.drawString(15, 84, "" + Util.toSiString(((ContainerMeter)this.container).getResultMin(), 6) + unit, 2157374, false);
        this.drawString(15, 100, Localization.translate("ic2.itemToolMEter.cycle", ((ContainerMeter)this.container).getResultCount() / 20), 2157374, false);
        this.drawString(39, 114, Localization.translate("ic2.itemToolMEter.mode.reset"), 2157374, false);
        switch (((ContainerMeter)this.container).getMode()) {
            case EnergyIn: {
                this.drawString(105, 100, Localization.translate("ic2.itemToolMEter.mode.EnergyIn"), 2157374, false);
                break;
            }
            case EnergyOut: {
                this.drawString(105, 100, Localization.translate("ic2.itemToolMEter.mode.EnergyOut"), 2157374, false);
                break;
            }
            case EnergyGain: {
                this.drawString(105, 100, Localization.translate("ic2.itemToolMEter.mode.EnergyGain"), 2157374, false);
                break;
            }
            case Voltage: {
                this.drawString(105, 100, Localization.translate("ic2.itemToolMEter.mode.Voltage"), 2157374, false);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.bindTexture();
        switch (((ContainerMeter)this.container).getMode()) {
            case EnergyIn: {
                this.drawTexturedRect(112.0, 55.0, 40.0, 40.0, 176.0, 0.0);
                break;
            }
            case EnergyOut: {
                this.drawTexturedRect(112.0, 55.0, 40.0, 40.0, 176.0, 40.0);
                break;
            }
            case EnergyGain: {
                this.drawTexturedRect(112.0, 55.0, 40.0, 40.0, 176.0, 120.0);
                break;
            }
            case Voltage: {
                this.drawTexturedRect(112.0, 55.0, 40.0, 40.0, 176.0, 80.0);
            }
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guitooleumeter.png");
    }
}

