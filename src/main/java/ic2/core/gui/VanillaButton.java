/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.resources.ResourceLocation
 */
package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.gui.Button;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.MouseButton;
import net.minecraft.resources.ResourceLocation;

public class VanillaButton
extends Button<VanillaButton> {
    protected IEnableHandler disableHandler;
    // 1.21 迁移修复（2026-09-23）：原版自 1.20.2 起已移除 textures/gui/widgets.png，改用 GUI sprite 体系。
    // 原实现从该贴图 9-slice 取样 v=46(disabled)/66(normal)/86(hover) 三段，1.21 下必然加载失败
    // （日志 "Failed to load texture: minecraft:textures/gui/widgets.png"）→ 按钮无底图。
    // 现直接引用原版 widget sprite：其自带 nine_slice 缩放定义（button.png.mcmeta），
    // GuiGraphics.blitSprite 会按九宫格自动拉伸，视觉与原版一致，且无需自备贴图。
    private static final ResourceLocation spriteDisabled = ResourceLocation.withDefaultNamespace("widget/button_disabled");
    private static final ResourceLocation spriteNormal = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation spriteHovered = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final int colorNormal = 0xE0E0E0;
    private static final int colorHover = 0xFFFFA0;
    private static final int colorDisabled = 0xA0A0A0;

    public VanillaButton(Ic2Gui<?> ic2Gui, int n, int n2, int n3, int n4, IClickHandler iClickHandler) {
        super(ic2Gui, n, n2, n3, n4, iClickHandler);
    }

    public VanillaButton withDisableHandler(IEnableHandler iEnableHandler) {
        this.disableHandler = iEnableHandler;
        return this;
    }

    public boolean isDisabled() {
        return this.disableHandler != null && !this.disableHandler.isEnabled();
    }

    @Override
    public void drawBackground(PoseStack poseStack, int n, int n2) {
        ResourceLocation resourceLocation;
        if (this.isDisabled()) {
            resourceLocation = spriteDisabled;
        } else if (!this.isActive(n, n2)) {
            resourceLocation = spriteNormal;
        } else {
            resourceLocation = spriteHovered;
        }
        // 注意：必须走 blitGuiSprite（内部补 leftPos/topPos）——GuiGraphics.blitSprite 用的是屏幕绝对坐标，
        // 直接调用会把按钮画到屏幕左上方向（2026-09-23 实测的 GUI 错位问题）。
        this.gui.blitGuiSprite(resourceLocation, this.x, this.y, this.width, this.height);
        super.drawBackground(poseStack, n, n2);
    }

    protected boolean isActive(int n, int n2) {
        return this.contains(n, n2);
    }

    @Override
    protected int getTextColor(int n, int n2) {
        return this.isDisabled() ? 0xA0A0A0 : (this.isActive(n, n2) ? 0xFFFFA0 : 0xE0E0E0);
    }

    @Override
    protected boolean onMouseClick(int n, int n2, MouseButton mouseButton) {
        return this.isDisabled() ? false : super.onMouseClick(n, n2, mouseButton);
    }
}
