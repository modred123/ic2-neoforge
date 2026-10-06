/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package ic2.core.block.wiring;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.wiring.ContainerElectricBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class GuiElectricBlock
extends Ic2Gui<ContainerElectricBlock> {
    private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guielectricblock.png");

    public GuiElectricBlock(final ContainerElectricBlock containerElectricBlock, Inventory inventory, Component component) {
        super(containerElectricBlock, inventory, component, 196);
        this.addElement(EnergyGauge.asBar(this, 79, 38, (Ic2TileEntity)containerElectricBlock.base));
        this.addElement((GuiElement<?>)((VanillaButton)new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon((java.util.function.Supplier<ItemStack>)new Supplier<ItemStack>(){

            public ItemStack get() {
                return new ItemStack((ItemLike)Items.REDSTONE);
            }
        })).withTooltip((java.util.function.Supplier<String>)new Supplier<String>(){

            public String get() {
                return ((TileEntityElectricBlock)containerElectricBlock.base).getRedstoneMode();
            }
        }));
    }

    @Override
    protected void drawForegroundLayer(PoseStack poseStack, int n, int n2) {
        super.drawForegroundLayer(poseStack, n, n2);
        // 1.21 迁移修复（2026-09-23 第二十轮）：能量区 4 行文本 y 坐标整体 +15 偏移，改回 1.12.2 原版值。
        // 依据：反编译 1.12.2 的 ic2/core/block/wiring/GuiElectricBlock.class（本 jar industrialcraft-2-2.8.221-ex112.jar），
        // 其 drawForegroundLayer 为：
        //   info.armor  : (8,  ySize - 126 + 3)  → 196-126+3 = 73   ← 高度相对量，1.21 已按同式算出 74，保留
        //   info.level  : (79, 25)
        //   数值 " "+e : (110,35)
        //   容量 "/"+cap: (110,45)
        //   info.output : (85, 60)
        // 而 1.21 当前写的是 40 / 50 / 60 / 75 —— 恰好每行 +15，与变压器（GuiTransformer）同源缺陷：
        // 能量槽显示框在贴图 y≈34..50，+15 后标签掉到框外、数值落到槽下方的空白区，视觉上"串行"。
        // 坐标系已交叉验证：1.12.2 GuiIC2.func_146979_b 与 1.21 Ic2Gui.renderLabels 传入的都是 GUI 相对坐标，
        // 不存在额外偏移，故直接照搬原版数值。
        this.drawString(poseStack, 8, 74, Localization.translate("ic2.EUStorage.gui.info.armor"), 0x404040);
        this.drawString(poseStack, 79, 25, Localization.translate("ic2.EUStorage.gui.info.level"), 0x404040);
        int n3 = (int)Math.min(((TileEntityElectricBlock)((ContainerElectricBlock)this.menu).base).energy.getEnergy(), ((TileEntityElectricBlock)((ContainerElectricBlock)this.menu).base).energy.getCapacity());
        this.drawString(poseStack, 110, 35, " " + n3, 0x404040);
        this.drawString(poseStack, 110, 45, "/" + (int)((TileEntityElectricBlock)((ContainerElectricBlock)this.menu).base).energy.getCapacity(), 0x404040);
        String string = Localization.translate("ic2.EUStorage.gui.info.output", ((TileEntityElectricBlock)((ContainerElectricBlock)this.menu).base).getOutput());
        this.drawString(poseStack, 85, 60, string, 0x404040);
    }

    @Override
    protected ResourceLocation getTexture() {
        return background;
    }
}

