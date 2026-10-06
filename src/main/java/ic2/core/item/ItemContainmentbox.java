/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.item.Rarity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.ItemIC2;
import ic2.core.item.tool.ContainerContainmentbox;
import ic2.core.item.tool.HandHeldContainmentbox;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

@NotClassic
public class ItemContainmentbox
extends ItemIC2
implements IHandHeldInventory {
    public ItemContainmentbox() {
        super(ItemName.containment_box);
        this.setMaxStackSize(1);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (!world.isClientSide) {
            IC2.platform.launchGui(player, this.getInventory(player, hand, stack));
        }
        return InteractionResultHolder.success(stack);
    }

    public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        HandHeldContainmentbox containmentBox;
        if (!player.level().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerContainmentbox && (containmentBox = (HandHeldContainmentbox)((ContainerContainmentbox)player.containerMenu).base).isThisContainer(stack)) {
            containmentBox.saveAsThrown(stack);
            player.closeContainer();
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public IHasGui getInventory(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        return new HandHeldContainmentbox(player, hand, stack, 12);
    }
}

