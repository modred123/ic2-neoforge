/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererLivingBase
 *  net.minecraft.client.renderer.entity.layers.LayerRenderer
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.inventory.EntityEquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.ChatFormatting
 *  net.minecraftforge.client.event.RenderLivingEvent$Post
 *  net.minecraftforge.client.event.RenderLivingEvent$Pre
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.fml.common.Loader
 *  net.minecraftforge.fml.common.LoaderState
 *  net.minecraftforge.fml.common.eventhandler.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.gameevent.TickEvent$Phase
 *  net.minecraftforge.fml.common.gameevent.TickEvent$PlayerTickEvent
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.armor.jetpack;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBackupElectricItemManager;
import ic2.api.item.IElectricItem;
import ic2.core.init.Localization;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.item.armor.jetpack.JetpackLogic;
import ic2.core.ref.ItemName;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.api.distmarker.Dist;

public class JetpackHandler
implements IBackupElectricItemManager {
    private static Map<Player, ItemStack> playerArmorBuffer = new WeakHashMap<Player, ItemStack>();
    private boolean internalHandlesCheck = false;
    static final ItemStack jetpack;
    public static JetpackHandler instance;

    private JetpackHandler() {
        NeoForge.EVENT_BUS.register((Object)this);
        ElectricItem.registerBackupManager(this);
    }

    public static void init() {
        instance = new JetpackHandler();
    }

    public static void setJetpackAttached(ItemStack stack, boolean value) {
        if (stack == null) {
            return;
        }
        if (!value) {
            if (StackUtil.getTag(stack) == null) {
                return;
            }
            // 1.21.1 修复（第二十七轮）：原 StackUtil.getTag(stack).remove(...) 改的是副本，
            // 删除被丢弃 ⇒ "hasIC2Jetpack" 标记永远摘不掉。改用活引用。
            net.minecraft.nbt.CompoundTag compoundTag = StackUtil.getOrCreateNbtData(stack);
            compoundTag.remove("hasIC2Jetpack");
            if (compoundTag.isEmpty()) {
                StackUtil.setTag(stack, null);
            }
        } else if (isChestEquippable(stack)) {
            StackUtil.getOrCreateNbtData(stack).putBoolean("hasIC2Jetpack", true);
        }
    }

    private static boolean isChestEquippable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    public static boolean hasJetpackAttached(ItemStack stack) {
        return stack != null && isChestEquippable(stack) && StackUtil.getTag(stack) != null && StackUtil.getTag(stack).getBoolean("hasIC2Jetpack");
    }

    public static boolean hasJetpack(ItemStack stack) {
        return stack != null && (JetpackHandler.hasJetpackAttached(stack) || stack.getItem() instanceof IJetpack);
    }

    public static IJetpack getJetpack(ItemStack stack) {
        assert (JetpackHandler.hasJetpack(stack));
        if (stack.getItem() instanceof IJetpack) {
            return (IJetpack)stack.getItem();
        }
        return (IJetpack)jetpack.getItem();
    }

    public static double getTransferLimit() {
        return ((IElectricItem)jetpack.getItem()).getTransferLimit(jetpack);
    }

    @Override
    public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (this.getTier(stack) > tier) {
            return 0.0;
        }
        if (!ignoreTransferLimit) {
            amount = Math.min(amount, JetpackHandler.getTransferLimit());
        }
        double charge = StackUtil.getTag(stack) != null ? StackUtil.getTag(stack).getDouble("charge") : 0.0;
        amount = Math.min(amount, this.getMaxCharge(stack) - charge);
        if (!simulate) {
            StackUtil.getOrCreateNbtData(stack).putDouble("charge", charge + amount);
        }
        return amount;
    }

    @Override
    public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
        if (externally || this.getTier(stack) > tier || StackUtil.getTag(stack) == null) {
            return 0.0;
        }
        if (!ignoreTransferLimit) {
            amount = Math.min(amount, JetpackHandler.getTransferLimit());
        }
        double charge = StackUtil.getTag(stack).getDouble("charge");
        amount = Math.min(amount, charge);
        if (!simulate) {
            // 1.21.1 修复（第二十七轮）：原实现用 StackUtil.getTag(stack)（副本）做 remove/putDouble，
            // 写入全部被丢弃 ⇒ 喷气背包放电后电量不减（燃料永不耗尽）。
            // 注意：不能只改 remove 分支，putDouble 分支同样落在副本上。改用活引用统一处理。
            net.minecraft.nbt.CompoundTag compoundTag = StackUtil.getOrCreateNbtData(stack);
            if ((charge -= amount) == 0.0) {
                compoundTag.remove("charge");
                if (compoundTag.isEmpty()) {
                    StackUtil.setTag(stack, null);
                }
            } else {
                compoundTag.putDouble("charge", charge);
            }
        }
        return amount;
    }

    @Override
    public double getStackCharge(ItemStack stack) {
        return this.getCharge(stack);
    }

    public double getCharge(ItemStack stack) {
        return this.discharge(stack, Double.MAX_VALUE, Integer.MAX_VALUE, true, false, true);
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
        return ElectricItem.manager.getMaxCharge(jetpack.copy());
    }

    @Override
    public boolean canUse(ItemStack stack, double amount) {
        return ElectricItem.rawManager.canUse(stack, amount);
    }

    @Override
    public boolean use(ItemStack stack, double amount, LivingEntity entity) {
        return ElectricItem.rawManager.use(stack, amount, entity);
    }

    @Override
    public void chargeFromArmor(ItemStack stack, LivingEntity entity) {
    }

    @Override
    public String getToolTip(ItemStack stack) {
        return ElectricItem.rawManager.getToolTip(stack);
    }

    @Override
    public int getTier(ItemStack stack) {
        return ElectricItem.manager.getTier(jetpack.copy());
    }

    @Override
    public synchronized boolean handles(ItemStack stack) {
        if (this.internalHandlesCheck) {
            return false;
        }
        this.internalHandlesCheck = true;
        boolean handle = JetpackHandler.hasJetpackAttached(stack) && ElectricItem.manager.getMaxCharge(stack) <= 0.0;
        this.internalHandlesCheck = false;
        return handle;
    }

    @SubscribeEvent
    public void tick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (JetpackHandler.hasJetpack(stack)) {
            JetpackLogic.onArmorTick(player.level(), player, stack, JetpackHandler.getJetpack(stack));
        }
        if (playerArmorBuffer.containsKey(player)) {
            ItemStack lastStack = playerArmorBuffer.get(player);
            if (StackUtil.isEmpty(lastStack) && JetpackHandler.hasJetpackAttached(lastStack) && StackUtil.isEmpty(stack)) {
                ItemStack newJetpack = jetpack.copy();
                double oldCharge = ElectricItem.manager.getCharge(lastStack);
                ElectricItem.manager.charge(newJetpack, oldCharge, Integer.MAX_VALUE, true, false);
                player.setItemSlot(EquipmentSlot.CHEST, newJetpack);
            }
            playerArmorBuffer.remove(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void tooltip(ItemTooltipEvent event) {
        if (JetpackHandler.hasJetpackAttached(event.getItemStack())) {
            event.getToolTip().add(net.minecraft.network.chat.Component.literal(ChatFormatting.YELLOW + Localization.translate("ic2.jetpackAttached")));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public void livingAttack(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getSource() != null && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) && JetpackHandler.hasJetpackAttached(player.getItemBySlot(EquipmentSlot.CHEST))) {
            playerArmorBuffer.put(player, player.getItemBySlot(EquipmentSlot.CHEST));
        }
    }

    static {
        jetpack = ItemName.jetpack_electric.getItemStack();
    }
}

