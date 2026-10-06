/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item.tool;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.init.Localization;
import ic2.core.item.tool.ContainerToolScanner;
import ic2.core.util.Tuple;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiToolScanner
extends Ic2Gui<ContainerToolScanner> {
    public GuiToolScanner(ContainerToolScanner containerToolScanner, Inventory inventory, Component component) {
        super(containerToolScanner, inventory, component, 230);
    }

    @Override
    protected void drawForegroundLayer(PoseStack poseStack, int n, int n2) {
        super.drawForegroundLayer(poseStack, n, n2);
        // 第二十八轮修复：1.19.2 把标注与列表整体下移了 32px（52/66），但贴图 guitoolscanner.png
        // 自 1.12.2 起未变（MD5 0CDD8A76…，黑色显示区为 y=17..144），下移后第 10 行文字会溢出到
        // 玩家背包槽位上。回到 1.12.2 权威坐标 20 / 34，正好落在显示区内且与右侧图标列不重叠。
        this.drawString(poseStack, 10, 20, Localization.translate("ic2.itemScanner.found"), 2157374);
        // 第四十轮诊断：把"服务端数据还没到"与"服务端确实扫到 0 条"分开显示 —— 用户一眼就能看出该查同步还是查判定。
        if (((ContainerToolScanner)this.menu).scanResults == null) {
            this.drawString(poseStack, 10, 34, "(no data from server yet)", 16776960);
        } else if (((ContainerToolScanner)this.menu).scanResults.isEmpty()) {
            this.drawString(poseStack, 10, 34, "(nothing found in range)", 16776960);
        } else {
            int n3 = 0;
            for (Tuple.T2<ItemStack, Integer> t2 : ((ContainerToolScanner)this.menu).scanResults) {
                String string = ((ItemStack)t2.a).getItem().getName((ItemStack)t2.a).getString();
                this.drawString(poseStack, 10, 34 + n3 * 11, t2.b + "x " + string, 5752026);
                if (++n3 != 10) continue;
                break;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        PoseStack poseStack = guiGraphics.pose();
        super.renderBg(guiGraphics, f, n, n2);
        if (((ContainerToolScanner)this.menu).scanResults != null) {
            int n3 = 0;
            for (Tuple.T2<ItemStack, Integer> t2 : ((ContainerToolScanner)this.menu).scanResults) {
                int n4 = 135 + (n3 & 1) * 15;
                this.drawItem(n4, 11 * n3 + 28, (ItemStack)t2.a);
                if (++n3 != 10) continue;
                break;
            }
        }
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guitoolscanner.png");
    }
}

