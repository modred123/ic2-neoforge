package ic2.core;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.ScrollDirection;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * IC2 GUI 基类（ex112 业务版），适配 1.21.1：
 * 继承 Ic2Gui（1.19.2 迁移的 1.21.1 渲染/事件体系），
 * 保留 ex112 子类使用的旧签名钩子（drawForegroundLayer(int,int) 等）与无 PoseStack 渲染重载。
 */
public abstract class GuiIC2<T extends ContainerBase<? extends Container>>
extends Ic2Gui<T> {
    protected final T container;

    public GuiIC2(T container) {
        this(container, 176, 166);
    }

    public GuiIC2(T container, int ySize) {
        this(container, 176, ySize);
    }

    public GuiIC2(T container, int xSize, int ySize) {
        super(container, container.getPlayer().getInventory(), Component.empty(), xSize, ySize);
        this.container = container;
    }

    // ==  == = 生命周期桥接：把 ex112 子类 override 的旧签名钩子接入 1.21.1 渲染管线 ==  == =

    // Ic2Gui.renderBg -> drawBackgroundAndTitle(PoseStack,float,int,int)
    // 桥接旧签名 drawGuiContainerBackgroundLayer(float,int,int)（GuiFluidBottler/GuiCanner override）
    @Override
    protected void drawBackgroundAndTitle(PoseStack poseStack, float f, int n, int n2) {
        super.drawBackgroundAndTitle(poseStack, f, n, n2);
        this.drawGuiContainerBackgroundLayer(f, n, n2);
    }

    /** 旧签名钩子：子类可 override 画额外背景（ex112 语义） */
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
    }

    // Ic2Gui.renderLabels -> drawForegroundLayer(PoseStack,int,int)
    // 桥接旧签名 drawForegroundLayer(int,int)（绝大多数子类 override）
    @Override
    protected void drawForegroundLayer(PoseStack poseStack, int n, int n2) {
        super.drawForegroundLayer(poseStack, n, n2);
        this.drawForegroundLayer(n, n2);
    }

    /** 旧签名钩子：子类 override 画前景文字（ex112 语义） */
    protected void drawForegroundLayer(int n, int n2) {
    }

    // ==  == = 事件桥接：1.21.1 新签名 -> 旧签名钩子 ==  == =

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        boolean handled = super.mouseClicked(d, d2, n);
        this.mouseClicked((int)d, (int)d2, n);
        return handled;
    }

    /** 旧签名钩子：子类可 override（GuiFluidRegulator/GuiFluidDistributor/GuiChunkLoader） */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        boolean handled = super.mouseDragged(d, d2, n, d3, d4);
        this.mouseClickMove((int)d, (int)d2, n, 0L);
        return handled;
    }

    /** 旧签名钩子：子类可 override */
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        boolean handled = super.mouseReleased(d, d2, n);
        this.mouseReleased((int)d, (int)d2, n);
        return handled;
    }

    /** 旧签名钩子：子类可 override */
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        boolean handled = super.mouseScrolled(d, d2, d3, d4);
        if (!handled) {
            ScrollDirection direction = d3 != 0.0 ? (d3 < 0.0 ? ScrollDirection.down : ScrollDirection.up) : ScrollDirection.stopped;
            this.handleMouseInput((int)d, (int)d2, direction);
        }
        return handled;
    }

    /** 旧签名钩子：子类可 override 处理滚动 */
    public void handleMouseInput(int mouseX, int mouseY, ScrollDirection direction) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (!handled) {
            this.keyTyped('\0', keyCode);
        }
        return handled;
    }

    @Override
    public boolean charTyped(char c, int n) {
        boolean handled = super.charTyped(c, n);
        if (!handled) {
            this.keyTyped(c, n);
        }
        return handled;
    }

    /** 旧签名钩子：子类可 override */
    public void keyTyped(char typedChar, int keyCode) {
    }

    // ==  == = 无 PoseStack 渲染兼容重载（ex112 子类直接调用） ==  == =

    public void drawTexturedRect(double x, double y, double width, double height, double texX, double texY) {
        this.drawTexturedRect(this.currentGuiGraphics.pose(), x, y, width, height, texX, texY);
    }

    public void drawTexturedRect(double x, double y, double width, double height, double texX, double texY, boolean mirrorX) {
        this.drawTexturedRect(this.currentGuiGraphics.pose(), x, y, width, height, texX, texY, mirrorX);
    }

    public void drawTexturedRect(double x, double y, double width, double height, double uS, double vS, double uE, double vE, boolean mirrorX) {
        this.drawTexturedRect(this.currentGuiGraphics.pose(), x, y, width, height, uS, vS, uE, vE, mirrorX);
    }

    public void drawSprite(double x, double y, double width, double height, TextureAtlasSprite sprite, int color, double scale, boolean fixRight, boolean fixBottom) {
        this.drawSprite(this.currentGuiGraphics.pose(), x, y, width, height, sprite, color, scale, fixRight, fixBottom);
    }

    @Override
    public void drawItem(int x, int y, ItemStack stack) {
        this.currentGuiGraphics.renderItem(stack, this.leftPos + x, this.topPos + y);
    }

    @Override
    public void drawItemStack(int x, int y, ItemStack stack) {
        this.drawItem(x, y, stack);
        this.currentGuiGraphics.renderItemDecorations(this.font, stack, this.leftPos + x, this.topPos + y);
    }

    public void drawColoredRect(int x, int y, int width, int height, int color) {
        this.drawColoredRect(this.currentGuiGraphics.pose(), x, y, width, height, color);
    }

    public int drawString(int x, int y, String text, int color, boolean shadow) {
        return this.drawString(this.currentGuiGraphics.pose(), x, y, text, color, shadow);
    }

    public void drawXCenteredString(int x, int y, String text, int color, boolean shadow) {
        this.drawXCenteredString(this.currentGuiGraphics.pose(), x, y, text, color, shadow);
    }

    public void drawXCenteredString(int x, int y, Component component, int color, boolean shadow) {
        this.drawXCenteredString(this.currentGuiGraphics.pose(), x, y, component, color, shadow);
    }

    public void drawXYCenteredString(int x, int y, String text, int color, boolean shadow) {
        this.drawXYCenteredString(this.currentGuiGraphics.pose(), x, y, text, color, shadow);
    }

    public void drawXYCenteredString(int x, int y, Component component, int color, boolean shadow) {
        this.drawXYCenteredString(this.currentGuiGraphics.pose(), x, y, component, color, shadow);
    }

    public void drawCenteredString(int x, int y, String text, int color, boolean shadow, boolean centerX, boolean centerY) {
        this.drawCenteredString(this.currentGuiGraphics.pose(), x, y, text, color, shadow, centerX, centerY);
    }

    public void drawCenteredString(int x, int y, Component component, int color, boolean shadow, boolean centerX, boolean centerY) {
        this.drawCenteredString(this.currentGuiGraphics.pose(), x, y, component, color, shadow, centerX, centerY);
    }

    public void drawTooltipStrings(int x, int y, List<String> text) {
        List<Component> components = new ArrayList<>();
        for (String line : text) {
            components.add(Component.literal(line));
        }
        this.drawTooltip(x, y, components);
    }

    public void drawTooltip(int x, int y, ItemStack stack) {
        assert (!StackUtil.isEmpty(stack));
        this.currentGuiGraphics.renderTooltip(this.font, stack, this.leftPos + x, this.topPos + y);
    }

    @Override
    protected IClickHandler createEventSender(final int event) {
        if (((ContainerBase)this.menu).base instanceof BlockEntity) {
            return new IClickHandler(){
                @Override
                public void onClick(MouseButton button) {
                    IC2.network.get(false).initiateClientTileEntityEvent((BlockEntity)((ContainerBase)GuiIC2.this.menu).base, event);
                }
            };
        }
        throw new IllegalArgumentException("not applicable for " + ((ContainerBase)this.menu).base);
    }
}
