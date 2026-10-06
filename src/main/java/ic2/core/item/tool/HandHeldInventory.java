/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.CrashReport
 *  net.minecraft.CrashReportCategory
 *  net.minecraft.CrashReportDetail
 *  net.minecraft.ReportedException
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class HandHeldInventory
implements IHasGui {
    protected ItemStack containerStack;
    protected final ItemStack[] inventory;
    public final Player player;
    protected final InteractionHand hand;
    private boolean cleared;
    private static final Set<Player> PLAYERS_IN_GUI = new HashSet<Player>();

    public HandHeldInventory(Player player, InteractionHand interactionHand, ItemStack itemStack, int n) {
        this.containerStack = itemStack;
        this.inventory = new ItemStack[n];
        this.player = player;
        this.hand = interactionHand;
        if (IC2.sideProxy.isSimulating()) {
            CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
            if (!compoundTag.contains("uid", 3)) {
                compoundTag.putInt("uid", IC2.random.nextInt());
            }
            ListTag listTag = compoundTag.getList("Items", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag2 = listTag.getCompound(i);
                byte by = compoundTag2.getByte("Slot");
                if (by < 0 || by >= this.inventory.length) continue;
                this.inventory[by] = ItemStack.parseOptional(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), compoundTag2);
            }
        }
    }

    public int getContainerSize() {
        return this.inventory.length;
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.inventory) {
            if (StackUtil.isEmpty(itemStack)) continue;
            return false;
        }
        return true;
    }

    public ItemStack getItem(int n) {
        return StackUtil.wrapEmpty(this.inventory[n]);
    }

    public ItemStack removeItem(int n, int n2) {
        ItemStack itemStack;
        if (n >= 0 && n < this.inventory.length && !StackUtil.isEmpty(itemStack = this.inventory[n])) {
            ItemStack itemStack2;
            if (n2 >= StackUtil.getSize(itemStack)) {
                itemStack2 = itemStack;
                this.inventory[n] = StackUtil.emptyStack;
            } else {
                itemStack2 = StackUtil.copyWithSize(itemStack, n2);
                this.inventory[n] = StackUtil.decSize(itemStack, n2);
            }
            this.save();
            return itemStack2;
        }
        return StackUtil.emptyStack;
    }

    public void setItem(int n, ItemStack itemStack) {
        if (!StackUtil.isEmpty(itemStack) && StackUtil.getSize(itemStack) > this.getMaxStackSize()) {
            itemStack = StackUtil.copyWithSize(itemStack, this.getMaxStackSize());
        }
        this.inventory[n] = StackUtil.isEmpty(itemStack) ? StackUtil.emptyStack : itemStack;
        this.save();
    }

    public int getMaxStackSize() {
        return 64;
    }

    public boolean canPlaceItem(int n, ItemStack itemStack) {
        return false;
    }

    public void setChanged() {
        this.save();
    }

    public boolean stillValid(Player player) {
        return player == this.player && this.getPlayerInventoryIndex() >= -1;
    }

    public ItemStack removeItemNoUpdate(int n) {
        ItemStack itemStack = this.getItem(n);
        if (!StackUtil.isEmpty(itemStack)) {
            this.setItem(n, null);
        }
        return itemStack;
    }

    @Override
    public void onScreenClosed(Player player) {
        this.save();
        if (!player.getCommandSenderWorld().isClientSide) {
            if (PLAYERS_IN_GUI.contains(player)) {
                PLAYERS_IN_GUI.remove(player);
            } else {
                StackUtil.getOrCreateNbtData(this.containerStack).remove("uid");
            }
        }
    }

    public ItemStack getContainerStack() {
        return this.containerStack;
    }

    public boolean isThisContainer(ItemStack itemStack) {
        if (StackUtil.isEmpty(itemStack) || itemStack.getItem() != this.containerStack.getItem()) {
            return false;
        }
        CompoundTag compoundTag = itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) == null ? null : itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
        return compoundTag != null && compoundTag.getInt("uid") == this.getUid();
    }

    protected int getUid() {
        CompoundTag compoundTag = StackUtil.getOrCreateNbtData(this.containerStack);
        return compoundTag.getInt("uid");
    }

    protected int getPlayerInventoryIndex() {
        ItemStack itemStack = this.player.containerMenu.getCarried();
        if (this.isThisContainer(itemStack)) {
            return -1;
        }
        for (int i = 0; i < this.player.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack2 = this.player.getInventory().getItem(i);
            if (!this.isThisContainer(itemStack2)) continue;
            return i;
        }
        return Integer.MIN_VALUE;
    }

    protected void save() {
        CompoundTag compoundTag;
        int n;
        if (!IC2.sideProxy.isSimulating()) {
            return;
        }
        if (this.cleared) {
            return;
        }
        boolean bl = false;
        for (int i = 0; i < this.inventory.length; ++i) {
            if (!this.isThisContainer(this.inventory[i])) continue;
            this.inventory[i] = null;
            bl = true;
        }
        ListTag listTag = new ListTag();
        for (n = 0; n < this.inventory.length; ++n) {
            if (StackUtil.isEmpty(this.inventory[n])) continue;
            compoundTag = new CompoundTag();
            compoundTag.putByte("Slot", (byte)n);
            this.inventory[n].save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), compoundTag);
            listTag.add(compoundTag);
        }
        StackUtil.getOrCreateNbtData(this.containerStack).put("Items", (Tag)listTag);
        try {
            this.containerStack = StackUtil.copyWithSize(this.containerStack, 1);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            CrashReport crashReport = new CrashReport("Hand held container stack vanished", (Throwable)illegalArgumentException);
            CrashReportCategory crashReportCategory = crashReport.addCategory("Container stack");
            crashReportCategory.setDetail("Stack", (Object)StackUtil.toStringSafe(this.containerStack));
            crashReportCategory.setDetail("NBT", (Object)(this.containerStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) == null ? null : this.containerStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag()));
            crashReportCategory.setDetail("Position", (Object)this.getPlayerInventoryIndex());
            crashReportCategory.setDetail("Had thrown", (Object)bl);
            crashReportCategory = crashReport.addCategory("Container info");
            crashReportCategory.setDetail("Type", (Object)this.getClass().getName());
            crashReportCategory.setDetail("Container", this.player.containerMenu == null ? null : this.player.containerMenu.getClass().getName());
            if (this.player.level().isClientSide) {
                crashReportCategory.setDetail("GUI", (CrashReportDetail)new CrashReportDetail<String>(){

                    public String call() throws Exception {
                        Screen screen = Minecraft.getInstance().screen;
                        return screen == null ? null : screen.getClass().getName();
                    }
                });
            }
            crashReportCategory.setDetail("Opened by", (Object)this.player);
            throw new ReportedException(crashReport);
        }
        if (bl) {
            StackUtil.dropAsEntity(this.player.getCommandSenderWorld(), this.player.blockPosition(), this.containerStack);
            this.clearContent();
        } else {
            n = this.getPlayerInventoryIndex();
            if (n < -1) {
                IC2.log.warn(LogCategory.Item, "Handheld inventory saving failed for player " + this.player.getDisplayName().getString() + ".");
                this.clearContent();
            } else if (n == -1) {
                this.player.containerMenu.setCarried(this.containerStack);
            } else {
                this.player.getInventory().setItem(n, this.containerStack);
            }
        }
    }

    public void saveAsThrown(ItemStack itemStack) {
        assert (IC2.sideProxy.isSimulating());
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.inventory.length; ++i) {
            if (StackUtil.isEmpty(this.inventory[i]) || this.isThisContainer(this.inventory[i])) continue;
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putByte("Slot", (byte)i);
            this.inventory[i].save(net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY), compoundTag);
            listTag.add(compoundTag);
        }
        StackUtil.getOrCreateNbtData(itemStack).put("Items", (Tag)listTag);
        assert (StackUtil.getOrCreateNbtData(itemStack).getInt("uid") == 0);
        this.clearContent();
    }

    public void clearContent() {
        for (int i = 0; i < this.inventory.length; ++i) {
            this.inventory[i] = null;
        }
        this.cleared = true;
    }

    public SlotHologramSlot.ChangeCallback makeSaveCallback() {
        return new SlotHologramSlot.ChangeCallback(){

            @Override
            public void onChanged(int n) {
                HandHeldInventory.this.save();
            }
        };
    }

    public void onEvent(String string) {
    }

    public static void addMaintainedPlayer(Player player) {
        PLAYERS_IN_GUI.add(player);
    }
}

