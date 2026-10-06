/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.Util;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IHasGui
extends Container {
    public ContainerBase<?> createServerScreenHandler(int var1, Player var2);

    default public void writeScreenOpenData(Player player, InteractionHand interactionHand, GrowingBuffer growingBuffer) throws IOException {
    }

    public ContainerBase<?> createClientScreenHandler(int var1, Inventory var2, GrowingBuffer var3);

    default public void onScreenClosed(Player player) {
    }

    default public boolean openManagedBe(Player player, InteractionHand interactionHand) {
        GrowingBuffer growingBuffer = new GrowingBuffer(50);
        try {
            Ic2ScreenHandlers.writeManagedBeData((BlockEntity)this, growingBuffer);
            this.writeScreenOpenData(player, interactionHand, growingBuffer);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer.flip();
        return IC2.envProxy.openHandledScreen(player, new MenuProvider(){

            public AbstractContainerMenu createMenu(int n, Inventory inventory, Player player) {
                return IHasGui.this.createServerScreenHandler(n, player);
            }

            public Component getDisplayName() {
                return IHasGui.getBeName((BlockEntity)IHasGui.this);
            }
        }, growingBuffer);
    }

    public static Component getBeName(BlockEntity blockEntity) {
        ResourceLocation resourceLocation = Util.getName(blockEntity.getBlockState().getBlock());
        return Component.translatable((String)String.format("container.%s.%s", resourceLocation.getNamespace(), resourceLocation.getPath().replace('/', '.')));
    }

    default public boolean openManagedItem(Player player, InteractionHand interactionHand, final Integer n) {
        GrowingBuffer growingBuffer = new GrowingBuffer(50);
        try {
            Ic2ScreenHandlers.writeManagedItemData(player, interactionHand, n, growingBuffer);
            this.writeScreenOpenData(player, interactionHand, growingBuffer);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer.flip();
        final Item item = player.getItemInHand(interactionHand).getItem();
        return IC2.envProxy.openHandledScreen(player, new MenuProvider(){

            public AbstractContainerMenu createMenu(int n2, Inventory inventory, Player player) {
                return IHasGui.this.createServerScreenHandler(n2, player);
            }

            public Component getDisplayName() {
                return IHasGui.getItemName(item, n);
            }
        }, growingBuffer);
    }

    public static Component getItemName(Item item, Integer n) {
        ResourceLocation resourceLocation = Util.getName(item);
        String string = String.format("container.%s.%s%s", resourceLocation.getNamespace(), resourceLocation.getPath().replace('/', '.'), n != null ? String.format(".%d", n) : "");
        /*
         * 第三十五轮修复：手持 GUI 的标题键 container.<ns>.<物品名>[.<n>] 常常没人补，
         * 缺键时原版会把 raw key 直接画在标题栏上（用户截图：OV扫描器标题显示
         * "container.ic2.advanced_scanner"，高级升级模块的子界面同理）。
         * 1.19.4+ 起 Component 支持带兜底文案的 translatable：缺键时退回"手上那件物品的名字"，
         * 与 Platform.launchGui 对所有手持 GUI 的标题策略保持一致（键存在时仍优先用本地化键值）。
         */
        return Component.translatableWithFallback((String)string, new ItemStack(item).getHoverName().getString());
    }
}

