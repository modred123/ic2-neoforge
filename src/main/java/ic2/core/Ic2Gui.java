/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferBuilder$RenderedBuffer
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  org.joml.Matrix4f
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.components.MultiLineLabel
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IKeyboardDependent;
import ic2.core.gui.MouseButton;
import ic2.core.gui.ScrollDirection;
import ic2.core.util.StackUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class Ic2Gui<T extends ContainerBase<? extends Container>>
extends AbstractContainerScreen<T> {
    public static final int textHeight = 8;
    protected static Runnable closeHandler;
    private boolean fixKeyEvents = false;
    protected GuiGraphics currentGuiGraphics;
    /**
     * 第三十四轮修复：当前是否处于**前景层**绘制。
     *
     * 1.21.1 原版 `AbstractContainerScreen.render` 的真实顺序（javap 实证，见下），决定了两个阶段的坐标系不同：
     * <pre>
     *   renderBackground → renderables(按钮等) → pushPose + translate(leftPos, topPos) → renderSlot
     *                    → renderLabels(=drawForegroundLayer) → popPose
     * </pre>
     * 也就是说：**背景层**（renderBg / drawBackgroundAndTitle / GuiElement#drawBackground）在**未平移**的坐标系里，
     * **前景层**（renderLabels / drawForegroundLayer / GuiElement#drawForeground）在**已平移**的坐标系里。
     * 而本工程所有 GUI 代码（继承 1.12.2 写法）一律传 GUI 相对坐标，所以底层绘制助手必须按阶段决定
     * 要不要手动补 (leftPos, topPos)：背景层补，前景层不补。
     *
     * 修复前助手无条件补偏移 → 前景层画出来的文字/元件整体偏移一倍。EU电表的绿色文字因此落到黑色显示框外，
     * 就是用户报的"UI 出现偏移"。
     */
    private boolean foregroundPhase = false;
    private final Set<GuiElement.ImplementedMethod> elementMethods = EnumSet.noneOf(GuiElement.ImplementedMethod.class);
    private final Queue<Tooltip> queuedTooltips = new ArrayDeque<Tooltip>();
    protected final List<GuiElement<?>> elements = new ArrayList();

    public Ic2Gui(T t, Inventory inventory, Component component) {
        this(t, inventory, component, 176, 166);
    }

    public Ic2Gui(T t, Inventory inventory, Component component, int n) {
        this(t, inventory, component, 176, n);
    }

    public Ic2Gui(T t, Inventory inventory, Component component, int n, int n2) {
        super(t, inventory, component);
        this.imageHeight = n2;
        this.imageWidth = n;
    }

    public final T getContainer() {
        return (T)((Object)((ContainerBase)this.menu));
    }

    public final int getX() {
        return this.leftPos;
    }

    public final int getY() {
        return this.topPos;
    }

    public final Slot getFocusedSlot() {
        return this.hoveredSlot;
    }

    public void init() {
        super.init();
        for (GuiElement<?> guiElement : this.elements) {
            if (!(guiElement instanceof IKeyboardDependent)) continue;
            this.fixKeyEvents = true;
            break;
        }
    }

    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderBackground(guiGraphics, n, n2, f);
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }

    public void containerTick() {
        super.containerTick();
        if (this.elementMethods.contains((Object)GuiElement.ImplementedMethod.tick)) {
            for (GuiElement<?> guiElement : this.elements) {
                if (!guiElement.isEnabled()) continue;
                guiElement.tick();
            }
        }
    }

    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        this.foregroundPhase = false;
        this.currentGuiGraphics = guiGraphics;
        PoseStack poseStack = guiGraphics.pose();
        this.drawBackgroundAndTitle(poseStack, f, n -= this.leftPos, n2 -= this.topPos);
        if (((ContainerBase)this.menu).base instanceof IUpgradableBlock) {
            Ic2Gui.bindTexture(ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/infobutton.png"));
            this.drawTexturedRect(poseStack, 3.0, 3.0, 10.0, 10.0, 0.0, 0.0);
        }
        if (this.elementMethods.contains((Object)GuiElement.ImplementedMethod.drawBackground)) {
            for (GuiElement<?> guiElement : this.elements) {
                if (!guiElement.isEnabled()) continue;
                guiElement.drawBackground(poseStack, n, n2);
            }
        }
    }

    protected void drawBackgroundAndTitle(PoseStack poseStack, float f, int n, int n2) {
        this.bindTexture();
        this.currentGuiGraphics.blit(this.getTexture(), this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        this.drawXCenteredString(poseStack, this.imageWidth / 2, 6, this.title, 0x404040, false);
    }

    protected final void renderLabels(GuiGraphics guiGraphics, int n, int n2) {
        this.currentGuiGraphics = guiGraphics;
        // 前景层：原版已 translate(leftPos, topPos)，故本阶段坐标 = 纯 GUI 相对坐标（助手不再补偏移）。
        this.foregroundPhase = true;
        PoseStack poseStack = guiGraphics.pose();
        this.drawForegroundLayer(poseStack, n - this.leftPos, n2 - this.topPos);
        this.flushTooltips(guiGraphics);
        // 复位：render() 里在本方法之后继续绘制的东西（若有）沿用"背景层"语义，与修复前一致。
        this.foregroundPhase = false;
    }

    protected void drawForegroundLayer(PoseStack poseStack, int n, int n2) {
        if (((ContainerBase)this.menu).base instanceof IUpgradableBlock) {
            this.handleUpgradeTooltip(n, n2);
        }
        for (GuiElement<?> guiElement : this.elements) {
            if (!guiElement.isEnabled()) continue;
            guiElement.drawForeground(poseStack, n, n2);
        }
    }

    private void handleUpgradeTooltip(int n, int n2) {
        if (n < 0 || n > 12 || n2 < 0 || n2 > 12) {
            return;
        }
        ArrayList<Component> arrayList = new ArrayList<Component>();
        arrayList.add((Component)Component.translatable((String)"ic2.generic.text.upgrade"));
        for (ItemStack itemStack : Ic2Gui.getCompatibleUpgrades((IUpgradableBlock)((ContainerBase)this.menu).base)) {
            arrayList.add((Component)itemStack.getHoverName().copy().withStyle(ChatFormatting.GRAY));
        }
        this.drawTooltip(n, n2, arrayList);
    }

    private static List<ItemStack> getCompatibleUpgrades(IUpgradableBlock iUpgradableBlock) {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        Set<UpgradableProperty> set = iUpgradableBlock.getUpgradableProperties();
        for (ItemStack itemStack : UpgradeRegistry.getUpgrades()) {
            IUpgradeItem iUpgradeItem = (IUpgradeItem)itemStack.getItem();
            if (!iUpgradeItem.isSuitableFor(itemStack, set)) continue;
            arrayList.add(itemStack);
        }
        return arrayList;
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.elementMethods.contains((Object)GuiElement.ImplementedMethod.onMouseScroll)) {
            ScrollDirection scrollDirection = d3 != 0.0 ? (d3 < 0.0 ? ScrollDirection.down : ScrollDirection.up) : ScrollDirection.stopped;
            for (GuiElement<?> guiElement : this.elements) {
                if (!guiElement.isEnabled() || !guiElement.contains((int)d, (int)d2)) continue;
                guiElement.onMouseScroll((int)d, (int)d2, scrollDirection);
            }
        }
        return super.mouseScrolled(d, d2, d3, d4);
    }

    public boolean mouseClicked(double d, double d2, int n) {
        MouseButton mouseButton;
        if (this.elementMethods.contains((Object)GuiElement.ImplementedMethod.onMouseClick) && (mouseButton = MouseButton.get(n)) != null) {
            boolean bl = false;
            d -= (double)this.leftPos;
            d2 -= (double)this.topPos;
            for (GuiElement<?> guiElement : this.elements) {
                if (!guiElement.isEnabled()) continue;
                bl |= guiElement.onMouseClick((int)d, (int)d2, mouseButton, guiElement.contains((int)d, (int)d2));
            }
            if (!bl) {
                d += (double)this.leftPos;
                d2 += (double)this.topPos;
            } else {
                return true;
            }
        }
        return super.mouseClicked(d, d2, n);
    }

    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        MouseButton mouseButton;
        if (this.elementMethods.contains((Object)GuiElement.ImplementedMethod.onMouseDrag) && (mouseButton = MouseButton.get(n)) != null) {
            boolean bl = false;
            d -= (double)this.leftPos;
            d2 -= (double)this.topPos;
            for (GuiElement<?> guiElement : this.elements) {
                if (!guiElement.isEnabled()) continue;
                bl |= guiElement.onMouseDrag((int)d, (int)d2, mouseButton, guiElement.contains((int)d, (int)d2));
            }
            if (!bl) {
                d += (double)this.leftPos;
                d2 += (double)this.topPos;
            } else {
                return true;
            }
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    public boolean mouseReleased(double d, double d2, int n) {
        MouseButton mouseButton;
        if (this.elementMethods.contains((Object)GuiElement.ImplementedMethod.onMouseRelease) && (mouseButton = MouseButton.get(n)) != null) {
            boolean bl = false;
            d -= (double)this.leftPos;
            d2 -= (double)this.topPos;
            for (GuiElement<?> guiElement : this.elements) {
                if (!guiElement.isEnabled()) continue;
                bl |= guiElement.onMouseRelease((int)d, (int)d2, mouseButton, guiElement.contains((int)d, (int)d2));
            }
            if (!bl) {
                d += (double)this.leftPos;
                d2 += (double)this.topPos;
            } else {
                return true;
            }
        }
        return super.mouseReleased(d, d2, n);
    }

    public boolean charTyped(char c, int n) {
        if (this.elementMethods.contains((Object)GuiElement.ImplementedMethod.onKeyTyped)) {
            boolean bl = false;
            for (GuiElement<?> guiElement : this.elements) {
                if (!guiElement.isEnabled()) continue;
                bl |= guiElement.onKeyTyped(c, n);
            }
            if (bl) {
                return true;
            }
        }
        return super.charTyped(c, n);
    }

    public void removed() {
        super.removed();
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    /** 底层绘制助手要补的 GUI 原点 X：背景层 = leftPos，前景层 = 0（pose 已平移）。 */
    protected final int guiOriginX() {
        return this.foregroundPhase ? 0 : this.leftPos;
    }

    /** 底层绘制助手要补的 GUI 原点 Y：背景层 = topPos，前景层 = 0（pose 已平移）。 */
    protected final int guiOriginY() {
        return this.foregroundPhase ? 0 : this.topPos;
    }

    public void drawTexturedRect(PoseStack poseStack, double d, double d2, double d3, double d4, double d5, double d6) {
        this.drawTexturedRect(poseStack, d, d2, d3, d4, d5, d6, false);
    }

    public void drawTexturedRect(PoseStack poseStack, double d, double d2, double d3, double d4, double d5, double d6, boolean bl) {
        this.drawTexturedRect(poseStack, d, d2, d3, d4, d5 / 256.0, d6 / 256.0, (d5 + d3) / 256.0, (d6 + d4) / 256.0, bl);
    }

    public void drawTexturedRect(PoseStack poseStack, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, boolean bl) {
        double d9 = (d += (double)this.guiOriginX()) + d3;
        double d10 = (d2 += (double)this.guiOriginY()) + d4;
        if (bl) {
            double d11 = d5;
            d5 = d7;
            d7 = d11;
        }
        Matrix4f matrix4f = poseStack.last().pose();
        int n = 0;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix4f, (float)d, (float)d2, (float)n).setUv((float)d5, (float)d6);
        bufferBuilder.addVertex(matrix4f, (float)d, (float)d10, (float)n).setUv((float)d5, (float)d8);
        bufferBuilder.addVertex(matrix4f, (float)d9, (float)d10, (float)n).setUv((float)d7, (float)d8);
        bufferBuilder.addVertex(matrix4f, (float)d9, (float)d2, (float)n).setUv((float)d7, (float)d6);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    public void drawSprite(PoseStack poseStack, double d, double d2, double d3, double d4, TextureAtlasSprite textureAtlasSprite, int n, double d5, boolean bl, boolean bl2) {
        if (textureAtlasSprite == null) {
            textureAtlasSprite = ((TextureAtlas)this.minecraft.getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS)).getSprite(MissingTextureAtlasSprite.getLocation());
        }
        d += this.guiOriginX();
        d2 += this.guiOriginY();
        double tileSize = d5 * 16.0;
        double u0 = textureAtlasSprite.getU0();
        double v0 = textureAtlasSprite.getV0();
        double du = (double)textureAtlasSprite.getU1() - u0;
        double dv = (double)textureAtlasSprite.getV1() - v0;
        int alpha = n >>> 24 & 255;
        int red = n >>> 16 & 255;
        int green = n >>> 8 & 255;
        int blue = n & 255;
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (double x = d; x < d + d3; x += tileSize) {
            double tileW = tileSize;
            double uStart = u0;
            if (x == d && bl) {
                tileW = d3 % tileSize;
                if (tileW > 0.0) {
                    uStart = u0 + du * (1.0 - tileW / tileSize);
                } else {
                    tileW = tileSize;
                }
            }
            double xEnd = Math.min(x + tileW, d + d3);
            double uEnd = uStart + (xEnd - x) / tileSize * du;
            for (double y = d2; y < d2 + d4; y += tileSize) {
                double tileH = tileSize;
                double vStart = v0;
                if (y == d2 && bl2) {
                    tileH = d4 % tileSize;
                    if (tileH > 0.0) {
                        vStart = v0 + dv * (1.0 - tileH / tileSize);
                    } else {
                        tileH = tileSize;
                    }
                }
                double yEnd = Math.min(y + tileH, d2 + d4);
                double vEnd = vStart + (yEnd - y) / tileSize * dv;
                bufferBuilder.addVertex(matrix4f, (float)x, (float)y, 0.0f).setUv((float)uStart, (float)vStart).setColor(red, green, blue, alpha);
                bufferBuilder.addVertex(matrix4f, (float)x, (float)yEnd, 0.0f).setUv((float)uStart, (float)vEnd).setColor(red, green, blue, alpha);
                bufferBuilder.addVertex(matrix4f, (float)xEnd, (float)yEnd, 0.0f).setUv((float)uEnd, (float)vEnd).setColor(red, green, blue, alpha);
                bufferBuilder.addVertex(matrix4f, (float)xEnd, (float)y, 0.0f).setUv((float)uEnd, (float)vStart).setColor(red, green, blue, alpha);
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    public void drawItem(int n, int n2, ItemStack itemStack) {
        this.currentGuiGraphics.renderItem(itemStack, this.guiOriginX() + n, this.guiOriginY() + n2);
    }

    public void drawItemStack(int n, int n2, ItemStack itemStack) {
        this.drawItem(n, n2, itemStack);
        this.currentGuiGraphics.renderItemDecorations(this.font, itemStack, this.guiOriginX() + n, this.guiOriginY() + n2);
    }

    public void drawColoredRect(PoseStack poseStack, int n, int n2, int n3, int n4, int n5) {
        int n6 = n5 >>> 24;
        boolean bl = n6 != 255 && n6 != 0;
        Matrix4f matrix4f = poseStack.last().pose();
        int n7 = (n += this.guiOriginX()) + n3;
        int n8 = (n2 += this.guiOriginY()) + n4;
        int n9 = 0;
        if (bl) {
            RenderSystem.enableBlend();
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix4f, (float)n, (float)n2, (float)n9).setColor(n5);
        bufferBuilder.addVertex(matrix4f, (float)n, (float)n8, (float)n9).setColor(n5);
        bufferBuilder.addVertex(matrix4f, (float)n7, (float)n8, (float)n9).setColor(n5);
        bufferBuilder.addVertex(matrix4f, (float)n7, (float)n2, (float)n9).setColor(n5);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        if (bl) {
            RenderSystem.disableBlend();
        }
    }

    /**
     * 前景层文字（1.12.2 语义）：**GUI 相对坐标、不画阴影**。
     *
     * 依据 1.12.2 源码：IC2 的 GUI 统一调 `fontRenderer.drawString(text, x, y, color)`（4 参重载），
     * 该重载内部等价于 `dropShadow = false`；而 1.21 的 `GuiGraphics.drawString(font, text, x, y, color)`
     * 默认 `shadow = true`，于是同一批文字平白多出一层 1px 暗影 —— 用户看到的"文字重影"
     * （储电箱 GUI 有、充电座 GUI 没有，差别就在这里：充电座走 TextLabel(..., shadow=false)）。
     */
    public int drawString(PoseStack poseStack, int n, int n2, String string, int n3) {
        return this.currentGuiGraphics.drawString(this.font, string, n, n2, n3, false);
    }

    public int drawString(PoseStack poseStack, int n, int n2, String string, int n3, boolean bl) {
        return this.currentGuiGraphics.drawString(this.font, string, this.guiOriginX() + n, this.guiOriginY() + n2, n3, bl) - this.guiOriginX();
    }

    public void drawTrimmedString(PoseStack poseStack, int n, int n2, String string, int n3, int n4) {
        MultiLineLabel.create(this.font, Component.literal(string), n3).renderLeftAligned(this.currentGuiGraphics, this.guiOriginX() + n, this.guiOriginY() + n2, 10, n4);
    }

    public void drawXCenteredString(PoseStack poseStack, int n, int n2, String string, int n3, boolean bl) {
        this.drawCenteredString(poseStack, n, n2, string, n3, bl, true, false);
    }

    public void drawXCenteredString(PoseStack poseStack, int n, int n2, Component component, int n3, boolean bl) {
        this.drawCenteredString(poseStack, n, n2, component, n3, bl, true, false);
    }

    public void drawXYCenteredString(PoseStack poseStack, int n, int n2, String string, int n3, boolean bl) {
        this.drawCenteredString(poseStack, n, n2, string, n3, bl, true, true);
    }

    public void drawXYCenteredString(PoseStack poseStack, int n, int n2, Component component, int n3, boolean bl) {
        this.drawCenteredString(poseStack, n, n2, component, n3, bl, true, true);
    }

    public void drawCenteredString(PoseStack poseStack, int n, int n2, String string, int n3, boolean bl, boolean bl2, boolean bl3) {
        if (bl2) {
            n -= this.getStringWidth(string) / 2;
        }
        if (bl3) {
            n2 -= 4;
        }
        this.currentGuiGraphics.drawString(this.font, string, this.guiOriginX() + n, this.guiOriginY() + n2, n3, bl);
    }

    public void drawCenteredString(PoseStack poseStack, int n, int n2, Component component, int n3, boolean bl, boolean bl2, boolean bl3) {
        if (bl2) {
            n -= this.getStringWidth(component) / 2;
        }
        if (bl3) {
            n2 -= 4;
        }
        this.currentGuiGraphics.drawString(this.font, component, this.guiOriginX() + n, this.guiOriginY() + n2, n3, bl);
    }

    public int getStringWidth(String string) {
        return this.font.width(string);
    }

    public int getStringWidth(Component component) {
        return this.font.width((FormattedText)component);
    }

    public String trimStringToWidth(String string, int n) {
        return this.font.plainSubstrByWidth(string, n, false);
    }

    public String trimStringToWidthReverse(String string, int n) {
        return this.font.plainSubstrByWidth(string, n, true);
    }

    public void drawTooltip(int n, int n2, List<Component> list) {
        this.queuedTooltips.add(new Tooltip(list, n, n2));
    }

    public void drawTooltip(PoseStack poseStack, int n, int n2, ItemStack itemStack) {
        assert (!StackUtil.isEmpty(itemStack));
        this.currentGuiGraphics.renderTooltip(this.font, itemStack, this.guiOriginX() + n, this.guiOriginY() + n2);
    }

    protected void flushTooltips(GuiGraphics guiGraphics) {
        for (Tooltip tooltip : this.queuedTooltips) {
            guiGraphics.renderTooltip(this.font, tooltip.text, Optional.empty(), tooltip.x, tooltip.y);
        }
        this.queuedTooltips.clear();
    }

    protected void addElement(GuiElement<?> guiElement) {
        this.elements.add(guiElement);
        this.elementMethods.addAll(guiElement.getImplementedMethods());
    }

    protected final void bindTexture() {
        Ic2Gui.bindTexture(this.getTexture());
    }

    public static void bindTexture(ResourceLocation resourceLocation) {
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)resourceLocation);
    }

    /**
     * 1.21 迁移补充（2026-09-23）：暴露当前帧的 GuiGraphics，供 GUI 元件使用 sprite 绘制 API。
     * 背景：1.20.2 起原版 GUI 改用 sprite/atlas 体系，直接绑定独立贴图（setShaderTexture）的老写法
     * 对已移除的资源（如 widgets.png）会失败；需要用 GuiGraphics.blitSprite 走 GUI atlas。
     *
     * 注意：{@link GuiGraphics#blitSprite} 用的是**屏幕绝对坐标**，而 IC2 的 GUI 元件拿到的 x/y
     * 都是相对 GUI 的（见 {@link #drawTexturedRect}，它内部会自动补 leftPos/topPos）。
     * 直接用 sprite 绘制元件请改用 {@link #blitGuiSprite}，否则元件会整体错位到屏幕左上方向。
     */
    public GuiGraphics getGuiGraphics() {
        return this.currentGuiGraphics;
    }

    /**
     * 用 **GUI 相对坐标**绘制 sprite（内部补 leftPos/topPos 屏幕偏移，语义与 drawTexturedRect 一致）。
     *
     * 教训（2026-09-23）：VanillaButton 改用 sprite 时漏了这个偏移，按钮底图画到了屏幕左上方向 →
     * 整个 GUI 呈现错位。凡是 sprite 绘制 GUI 元件，务必走本方法。
     */
    public void blitGuiSprite(ResourceLocation resourceLocation, int n, int n2, int n3, int n4) {
        this.currentGuiGraphics.blitSprite(resourceLocation, this.guiOriginX() + n, this.guiOriginY() + n2, n3, n4);
    }

    protected IClickHandler createEventSender(final int n) {
        if (((ContainerBase)this.menu).base instanceof BlockEntity) {
            return new IClickHandler(){

                @Override
                public void onClick(MouseButton mouseButton) {
                    IC2.network.get(false).initiateClientTileEntityEvent((BlockEntity)((ContainerBase)((Ic2Gui)Ic2Gui.this).menu).base, n);
                }
            };
        }
        throw new IllegalArgumentException("not applicable for " + ((ContainerBase)this.menu).base);
    }

    protected abstract ResourceLocation getTexture();

    private static class Tooltip {
        final int x;
        final int y;
        final List<Component> text;

        Tooltip(List<Component> list, int n, int n2) {
            this.text = list;
            this.x = n;
            this.y = n2;
        }
    }
}

