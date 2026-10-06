/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 */
package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.gui.BasicButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.init.Localization;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiAdvMiner
extends Ic2Gui<ContainerAdvMiner> {
    public GuiAdvMiner(final ContainerAdvMiner containerAdvMiner, Inventory inventory, Component component) {
        super(containerAdvMiner, inventory, component, 203);
        this.addElement(EnergyGauge.asBolt(this, 12, 55, (Ic2TileEntity)containerAdvMiner.base));
        this.addElement((GuiElement<?>)BasicButton.create(this, 133, 101, this.createEventSender(0), BasicButton.ButtonStyle.AdvMinerReset).withTooltip("ic2.AdvMiner.gui.switch.reset"));
        this.addElement((GuiElement<?>)BasicButton.create(this, 123, 27, this.createEventSender(1), BasicButton.ButtonStyle.AdvMinerMode).withTooltip("ic2.AdvMiner.gui.switch.mode"));
        this.addElement((GuiElement<?>)BasicButton.create(this, 129, 45, this.createEventSender(2), BasicButton.ButtonStyle.AdvMinerSilkTouch).withTooltip((java.util.function.Supplier<String>)new Supplier<String>(){

            public String get() {
                return Localization.translate("ic2.AdvMiner.gui.switch.silktouch", ((TileEntityAdvMiner)containerAdvMiner.base).silkTouch);
            }
        }));
    }

    @Override
    protected void drawForegroundLayer(PoseStack poseStack, int n, int n2) {
        // 第三十五轮修复：文字坐标是 1.19.2 的 (28,124)/(40,50)，比 1.12.2 原版各多 +19，
        // 结果两行绿字都画到了槽位区（用户截图：模式文字压在格子上）。
        // 权威值取自 1.12.2 `ic2_src_112/.../GuiAdvMiner`：(28,105) 与 (40,31)；
        // 并对 `guiadvminer.png` 逐点采样验证：(28,105)/(40,31) 黑底占比 100%（在显示框内），
        // 而 (28,124)/(40,50) 黑底占比 0%（在框外）。该贴图自 1.12.2 起未变。
        BlockPos blockPos = ((TileEntityAdvMiner)((ContainerAdvMiner)this.menu).base).getMineTarget();
        if (blockPos != null) {
            BlockPos blockPos2 = ((TileEntityAdvMiner)((ContainerAdvMiner)this.menu).base).getBlockPos();
            this.drawString(poseStack, 28, 105, Localization.translate("ic2.AdvMiner.gui.info.minelevel", blockPos.getX() - blockPos2.getX(), blockPos.getZ() - blockPos2.getZ(), blockPos.getY() - blockPos2.getY()), 2157374);
        }
        if (((TileEntityAdvMiner)((ContainerAdvMiner)this.menu).base).blacklist) {
            this.drawString(poseStack, 40, 31, Localization.translate("ic2.AdvMiner.gui.mode.blacklist"), 2157374);
        } else {
            this.drawString(poseStack, 40, 31, Localization.translate("ic2.AdvMiner.gui.mode.whitelist"), 2157374);
        }
        super.drawForegroundLayer(poseStack, n, n2);
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiadvminer.png");
    }
}

