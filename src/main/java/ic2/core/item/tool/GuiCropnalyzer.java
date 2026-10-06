/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.tool;
import net.neoforged.api.distmarker.OnlyIn;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.item.tool.ContainerCropnalyzer;
import ic2.core.item.tool.HandHeldCropnalyzer;
import ic2.core.ref.ItemName;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class GuiCropnalyzer
extends GuiIC2<ContainerCropnalyzer> {
    private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/GUICropnalyzer.png");

    public GuiCropnalyzer(ContainerCropnalyzer container) {
        super(container, 223);
        this.addElement(Text.create(this, 74, 11, ItemName.cropnalyzer.getItemStack().getDisplayName().getString(), 0, false));
        this.addElement((GuiElement<?>)Text.create(this, 8, 37, "UNKNOWN", 0xFFFFFF, false).withEnableHandler(() -> ((HandHeldCropnalyzer)container.base).getScannedLevel() == 0));
        this.addElement((GuiElement<?>)Text.create(this, 8, 37, this.cropSensitiveText((Supplier<String>)((Supplier)((HandHeldCropnalyzer)container.base)::getSeedName)), 0xFFFFFF, false).withEnableHandler(this.atLeastLevel(1)));
        IEnableHandler atLeast2 = this.atLeastLevel(2);
        this.addElement((GuiElement<?>)Text.create(this, 8, 50, this.cropSensitiveText((Supplier<String>)((Supplier)() -> "Tier: " + ((HandHeldCropnalyzer)container.base).getSeedTier())), 0xFFFFFF, false).withEnableHandler(atLeast2));
        this.addElement((GuiElement<?>)Text.create(this, 8, 73, "Discovered by:", 0xFFFFFF, false).withEnableHandler(atLeast2));
        this.addElement((GuiElement<?>)Text.create(this, 8, 86, this.cropSensitiveText((Supplier<String>)((Supplier)((HandHeldCropnalyzer)container.base)::getSeedDiscovered)), 0xFFFFFF, false).withEnableHandler(atLeast2));
        IEnableHandler atLeast3 = this.atLeastLevel(3);
        this.addElement((GuiElement<?>)Text.create(this, 8, 109, this.cropSensitiveText((Supplier<String>)((Supplier)() -> ((HandHeldCropnalyzer)container.base).getSeedDesc(0))), 0xFFFFFF, false).withEnableHandler(atLeast3));
        this.addElement((GuiElement<?>)Text.create(this, 8, 122, this.cropSensitiveText((Supplier<String>)((Supplier)() -> ((HandHeldCropnalyzer)container.base).getSeedDesc(1))), 0xFFFFFF, false).withEnableHandler(atLeast3));
        IEnableHandler atLeast4 = this.atLeastLevel(4);
        this.addElement((GuiElement<?>)Text.create(this, 118, 37, "Growth:", 11403055, false).withEnableHandler(atLeast4));
        this.addElement((GuiElement<?>)Text.create(this, 118, 50, this.cropSensitiveText((Supplier<String>)((Supplier)() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedGrowth()))), 11403055, false).withEnableHandler(atLeast4));
        this.addElement((GuiElement<?>)Text.create(this, 118, 73, "Gain:", 15649024, false).withEnableHandler(atLeast4));
        this.addElement((GuiElement<?>)Text.create(this, 118, 86, this.cropSensitiveText((Supplier<String>)((Supplier)() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedGain()))), 15649024, false).withEnableHandler(atLeast4));
        this.addElement((GuiElement<?>)Text.create(this, 118, 109, "Resis.:", 52945, false).withEnableHandler(atLeast4));
        this.addElement((GuiElement<?>)Text.create(this, 118, 122, this.cropSensitiveText((Supplier<String>)((Supplier)() -> Integer.toString(((HandHeldCropnalyzer)container.base).getSeedResistence()))), 52945, false).withEnableHandler(atLeast4));
    }

    /** 第三十三轮：1.21.1 的 `ClientEnvProxy.ScreenFactory` 需要三参构造器（见 GuiToolMeter 说明）。 */
    public GuiCropnalyzer(ContainerCropnalyzer containerCropnalyzer, Inventory inventory, Component component) {
        this(containerCropnalyzer);
    }

    private IEnableHandler atLeastLevel(int level) {
        return () -> ((HandHeldCropnalyzer)((ContainerCropnalyzer)this.container).base).getScannedLevel() >= level;
    }

    private TextProvider.ITextProvider cropSensitiveText(Supplier<String> text) {
        return TextProvider.of((Supplier<String>)((Supplier)() -> ((HandHeldCropnalyzer)((ContainerCropnalyzer)this.container).base).getScannedLevel() > -1 ? (String)text.get() : ""));
    }

    @Override
    protected ResourceLocation getTexture() {
        return background;
    }
}

