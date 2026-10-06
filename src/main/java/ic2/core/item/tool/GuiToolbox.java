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
import ic2.core.gui.Text;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.ref.ItemName;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class GuiToolbox
extends GuiIC2<ContainerToolbox> {
    private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/GUIToolbox.png");

    public GuiToolbox(ContainerToolbox container) {
        super(container);
        this.addElement(Text.create(this, 65, 11, ItemName.tool_box.getItemStack().getDisplayName().getString(), 0, false));
    }

    /** 第三十三轮：1.21.1 的 `ClientEnvProxy.ScreenFactory` 需要三参构造器（见 GuiToolMeter 说明）。 */
    public GuiToolbox(ContainerToolbox containerToolbox, Inventory inventory, Component component) {
        this(containerToolbox);
    }

    @Override
    protected ResourceLocation getTexture() {
        return background;
    }
}

