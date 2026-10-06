/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.NonNullList
 *  net.minecraft.ChatFormatting
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.ItemBattery;
import ic2.core.item.tool.GuiToolbox;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

@NotClassic
public class ItemBatteryChargeHotbar
extends ItemBattery
implements IBoxable {
    public ItemBatteryChargeHotbar(ItemName name, double maxCharge, double transferLimit, int tier) {
        super(name, maxCharge, transferLimit, tier);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag advanced) {
        Mode mode = this.getMode(stack);
        list.add(Component.literal(this.getNameOfMode(mode)));
        if (Minecraft.getInstance().screen instanceof GuiToolbox) {
            list.add(Component.literal((mode.enabled ? ChatFormatting.RED : ChatFormatting.GREEN) + Localization.translate("ic2.tooltip.mode.boxable")));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        Mode mode = this.getMode(stack);
        if (entity instanceof ServerPlayer && world.getGameTime() % 10L < (long)this.getTier(stack) && mode.enabled) {
            Player thePlayer = (Player)entity;
            NonNullList<ItemStack> inventory = thePlayer.getInventory().items;
            double limit = this.getTransferLimit(stack);
            int tier = this.getTier(stack);
            for (int i = 0; i < 9 && limit > 0.0; ++i) {
                ItemStack toCharge = inventory.get(i);
                if (StackUtil.isEmpty(toCharge) || mode == Mode.NOT_IN_HAND && i == thePlayer.getInventory().selected || toCharge.getItem() instanceof ItemBatteryChargeHotbar) continue;
                double charge = ElectricItem.manager.charge(toCharge, limit, tier, false, true);
                charge = ElectricItem.manager.discharge(stack, charge, tier, true, false, false);
                ElectricItem.manager.charge(toCharge, charge, tier, true, false);
                limit -= charge;
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (world.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        Mode mode = this.getMode(stack);
        mode = Mode.values[(mode.ordinal() + 1) % Mode.values.length];
        this.setMode(stack, mode);
        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.mode", this.getNameOfMode(mode)), new Object[0]);
        return InteractionResultHolder.success(stack);
    }

    private String getNameOfMode(Mode mode) {
        return Localization.translate("ic2.tooltip.mode." + mode.toString().toLowerCase(Locale.ENGLISH));
    }

    public void setMode(ItemStack stack, Mode mode) {
        CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
        nbt.putByte("mode", (byte)mode.ordinal());
    }

    public Mode getMode(ItemStack stack) {
        CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
        if (!nbt.contains("mode")) {
            return Mode.ENABLED;
        }
        return this.getMode(nbt.getByte("mode"));
    }

    private Mode getMode(int mode) {
        if (mode < 0 || mode >= Mode.values.length) {
            mode = 0;
        }
        return Mode.values[mode];
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemstack) {
        return this.getMode(itemstack) == Mode.DISABLED;
    }

    private static enum Mode {
        ENABLED(true),
        DISABLED(false),
        NOT_IN_HAND(true);

        private boolean enabled;
        public static final Mode[] values;

        private Mode(boolean enabled) {
            this.enabled = enabled;
        }

        static {
            values = Mode.values();
        }
    }
}

