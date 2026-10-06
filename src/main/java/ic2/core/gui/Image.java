/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.resources.ResourceLocation
 */
package ic2.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.gui.GlTexture;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IOverlaySupplier;
import ic2.core.gui.OverlaySupplier;
import net.minecraft.resources.ResourceLocation;

public class Image
extends GuiElement<Image> {
    private final ResourceLocation texture;
    private final int baseWidth;
    private final int baseHeight;
    private final IOverlaySupplier overlay;
    private final boolean autoWidth;
    private final boolean autoHeight;

    public static Image create(Ic2Gui<?> ic2Gui, int n, int n2, int n3, int n4, ResourceLocation resourceLocation, int n5, int n6, int n7, int n8, int n9, int n10) {
        return Image.create(ic2Gui, n, n2, n3, n4, resourceLocation, n5, n6, new OverlaySupplier(n7, n8, n9, n10));
    }

    public static Image create(Ic2Gui<?> ic2Gui, int n, int n2, int n3, int n4, ResourceLocation resourceLocation, int n5, int n6, IOverlaySupplier iOverlaySupplier) {
        boolean bl;
        boolean bl2 = n3 < 0;
        boolean bl3 = bl = n4 < 0;
        if (bl2) {
            n3 = 0;
        }
        if (bl) {
            n4 = 0;
        }
        return new Image(ic2Gui, n, n2, n3, n4, resourceLocation, n5, n6, iOverlaySupplier, bl2, bl);
    }

    protected Image(Ic2Gui<?> ic2Gui, int n, int n2, int n3, int n4, ResourceLocation resourceLocation, int n5, int n6, IOverlaySupplier iOverlaySupplier, boolean bl, boolean bl2) {
        super(ic2Gui, n, n2, n3, n4);
        if (resourceLocation == null) {
            throw new NullPointerException("null texture");
        }
        if (iOverlaySupplier == null) {
            throw new NullPointerException("null overlay");
        }
        this.texture = resourceLocation;
        this.baseWidth = n5;
        this.baseHeight = n6;
        this.overlay = iOverlaySupplier;
        this.autoWidth = bl;
        this.autoHeight = bl2;
    }

    /** 诊断（2026-09-01）：打印 Image 元素的真实绘制参数，用于定位太阳能太阳图标偏移。 */
    private static final java.util.concurrent.atomic.AtomicInteger diagDrawCount = new java.util.concurrent.atomic.AtomicInteger(0);

    @Override
    public void drawBackground(PoseStack poseStack, int n, int n2) {
        super.drawBackground(poseStack, n, n2);
        GlTexture glTexture = GlTexture.get(this.texture);
        if (glTexture != null) {
            double d;
            if (this.autoWidth) {
                this.width = glTexture.getWidth();
            }
            if (this.autoHeight) {
                this.height = glTexture.getHeight();
            }
            double d2 = this.baseWidth > 0 ? 1.0 / (double)this.baseWidth : 1.0 / (double)glTexture.getCanvasWidth();
            double d3 = this.baseHeight > 0 ? 1.0 / (double)this.baseHeight : 1.0 / (double)glTexture.getCanvasHeight();
            double d4 = this.overlay.getUS();
            double d5 = this.overlay.getVS();
            double d6 = this.overlay.getUE();
            if (d6 < 0.0) {
                d6 = d4 + (double)this.width;
            }
            if ((d = (double)this.overlay.getVE()) < 0.0) {
                d = d5 + (double)this.height;
            }
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)this.texture);
            // 诊断（2026-09-01）：打印真实绘制参数。太阳能 GUI 里有两个 Image 画在同一 (81,45)：
            // 帧0（灰框，u=0）与帧1（太阳，u=14）。若二者此行输出不同，即为偏移根因。
            int diagIdx = diagDrawCount.incrementAndGet();
            if (diagIdx <= 12) {
                org.apache.logging.log4j.LogManager.getLogger("ic2-diag").info(
                    "image draw #{} tex={} x={} y={} w={} h={} autoW={} autoH={} base={}x{} uvPx=[{},{}..{},{}] uvNorm=[{},{},{},{}] canvas={}x{}",
                    diagIdx, this.texture, this.x, this.y, this.width, this.height,
                    this.autoWidth, this.autoHeight, this.baseWidth, this.baseHeight,
                    d4, d5, d6, d, d4 * d2, d5 * d3, d6 * d2, d * d3,
                    glTexture.getCanvasWidth(), glTexture.getCanvasHeight());
            }
            this.gui.drawTexturedRect(poseStack, this.x, this.y, this.width, this.height, d4 * d2, d5 * d3, d6 * d2, d * d3, false);
        } else {
            if (this.autoWidth) {
                this.width = 0;
            }
            if (this.autoHeight) {
                this.height = 0;
            }
        }
    }
}

