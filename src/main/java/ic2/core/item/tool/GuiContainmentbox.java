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
import ic2.core.item.tool.ContainerContainmentbox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class GuiContainmentbox
extends GuiIC2<ContainerContainmentbox> {
    public GuiContainmentbox(ContainerContainmentbox container) {
        super(container);
    }

    /** 第三十三轮：1.21.1 的 `ClientEnvProxy.ScreenFactory` 需要三参构造器（见 GuiToolMeter 说明）。 */
    public GuiContainmentbox(ContainerContainmentbox containerContainmentbox, Inventory inventory, Component component) {
        this(containerContainmentbox);
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/GUIContainmentbox.png");
    }
}

