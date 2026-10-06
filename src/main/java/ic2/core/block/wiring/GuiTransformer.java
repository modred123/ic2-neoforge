/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package ic2.core.block.wiring;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.block.wiring.ContainerTransformer;
import ic2.core.block.wiring.tileentity.TileEntityTransformer;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TextLabel;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class GuiTransformer
extends Ic2Gui<ContainerTransformer> {
    public String[] mode = new String[]{"", "", "", ""};
    private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guitransfomer.png");

    public GuiTransformer(ContainerTransformer containerTransformer, Inventory inventory, Component component) {
        super(containerTransformer, inventory, component, 219);
        // 1.21 迁移修复（2026-09-23 第二轮）：能量文本 y 坐标从 56/70/56/72 改回 1.12.2 原版 30/43/30/45。
        // 依据：对 guitransfomer.png 做像素级扫描（该贴图在 1.12.2 / 1.18.2 / 1.19.2 三个版本 MD5 均为
        // 9ee59649b4c3dd52772e600a6612a573，从未变过），两个能量显示黑框的实际范围是：
        //   输出框 y=26..38、输入框 y=42..54、两者共用 x=47..142
        // 而 1.18+ 官方源码沿用至今的 56/70/56/72 全部落在显示框下方，并与按钮区（y=65/85/105）重叠，
        // 于是出现两个症状：显示框内空空如也、"输入:" 标签被按钮底图盖住。
        // 坐标系已交叉验证一致：1.12.2 GuiIC2.func_146979_b 传入 mouseY-guiTop，1.21 Ic2Gui.renderLabels
        // 传入 mouseY-topPos，两者都是 GUI 相对坐标，不存在额外偏移，故直接照搬原版数值。
        this.addElement(TextLabel.create(this, 6, 30, TextProvider.ofTranslated("ic2.Transformer.gui.Output"), 0x404040, true));
        this.addElement(TextLabel.create(this, 6, 43, TextProvider.ofTranslated("ic2.Transformer.gui.Input"), 0x404040, true));
        this.addElement(TextLabel.create(this, 52, 30, TextProvider.of(this::getOutputText), 2157374, true));
        this.addElement(TextLabel.create(this, 52, 45, TextProvider.of(this::getInputText), 2157374, true));
        this.addElement((GuiElement<?>)new VanillaButton(this, 7, 65, 144, 20, this.createEventSender(0)).withText(Localization.translate("ic2.Transformer.gui.switch.mode1")));
        this.addElement((GuiElement<?>)new VanillaButton(this, 7, 85, 144, 20, this.createEventSender(1)).withText(Localization.translate("ic2.Transformer.gui.switch.mode2")));
        this.addElement((GuiElement<?>)new VanillaButton(this, 7, 105, 144, 20, this.createEventSender(2)).withText(Localization.translate("ic2.Transformer.gui.switch.mode3")));
    }

    /**
     * 模式扳手的绘制——**完全复刻 1.12.2 的 drawForegroundLayer 写法**（2026-09-23 第二十轮）。
     *
     * 1.12.2 原版（GuiTransformer.java）：
     * <pre>
     *   RenderItem renderItem = this.field_146297_k.func_175599_af();
     *   RenderHelper.func_74520_c();
     *   switch (((TileEntityTransformer)((ContainerTransformer)this.container).base).getMode()) {
     *       case redstone: renderItem.func_175042_a(ItemName.wrench.getItemStack(), 152, 67);  break;
     *       case stepdown: renderItem.func_175042_a(ItemName.wrench.getItemStack(), 152, 87);  break;
     *       case stepup:   renderItem.func_175042_a(ItemName.wrench.getItemStack(), 152, 107);
     *   }
     *   RenderHelper.func_74518_a();
     * </pre>
     *
     * 此前 1.21 侧沿用了 1.19.2 的"三个固定位置 ItemImage + withEnableHandler"元素化写法
     * （元素在 renderBg **背景层**绘制）。两者在渲染层级上并不等价：1.12.2 画在**前景层**，
     * 而前景层绘制时 pose 已被原版 translate(leftPos, topPos)，坐标系为纯 GUI 相对坐标。
     * 为消除这一层级差异带来的不确定行为，此处按用户要求直接复刻原版逻辑——每帧读取
     * getMode() 现算现画，不依赖 enableHandler 的可见性判定。
     *
     * 坐标说明：本方法由 Ic2Gui.renderLabels 调用，此时 pose 已经过原版
     * AbstractContainerScreen 的 translate(leftPos, topPos)，故 152/67 等数值
     * 与 1.12.2 源码**逐字一致**、无需再补 leftPos/topPos（补了反而会偏移一倍）。
     */
    @Override
    protected void drawForegroundLayer(PoseStack poseStack, int n, int n2) {
        super.drawForegroundLayer(poseStack, n, n2);
        switch (((TileEntityTransformer)((ContainerTransformer)this.menu).base).getMode()) {
            case redstone -> this.getGuiGraphics().renderItem(getWrenchStack(), 152, 67);
            case stepdown -> this.getGuiGraphics().renderItem(getWrenchStack(), 152, 87);
            case stepup -> this.getGuiGraphics().renderItem(getWrenchStack(), 152, 107);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return background;
    }

    private boolean isStepUpMode() {
        return ((TileEntityTransformer)((ContainerTransformer)this.menu).base).getMode() == TileEntityTransformer.Mode.stepup;
    }

    private boolean isStepDownMode() {
        return ((TileEntityTransformer)((ContainerTransformer)this.menu).base).getMode() == TileEntityTransformer.Mode.stepdown;
    }

    private boolean isRedstoneMode() {
        return ((TileEntityTransformer)((ContainerTransformer)this.menu).base).getMode() == TileEntityTransformer.Mode.redstone;
    }

    private static ItemStack getWrenchStack() {
        return new ItemStack((ItemLike)Ic2Items.WRENCH);
    }

    private String getInputText() {
        return ((TileEntityTransformer)((ContainerTransformer)this.menu).base).getinputflow() + " " + Localization.translate("ic2.generic.text.EUt");
    }

    private String getOutputText() {
        return ((TileEntityTransformer)((ContainerTransformer)this.menu).base).getoutputflow() + " " + Localization.translate("ic2.generic.text.EUt");
    }
}

