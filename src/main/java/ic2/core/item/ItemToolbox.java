/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.ItemMeshDefinition
 *  net.minecraft.client.renderer.block.model.ModelBakery
 *  net.minecraft.client.renderer.block.model.ModelResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.item.Rarity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.model.ModelLoader
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.ItemIC2;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.tool.HandHeldToolbox;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;


import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;

public class ItemToolbox
extends ItemIC2
implements IHandHeldInventory {
    public ItemToolbox() {
        super(ItemName.tool_box);
        this.setMaxStackSize(1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(final ItemName name) {
        
        
        
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (IC2.platform.isSimulating()) {
            IC2.platform.launchGui(player, this.getInventory(player, hand, stack));
        }
        return InteractionResultHolder.success(stack);
    }

    public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        HandHeldToolbox toolbox;
        if (!player.level().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerToolbox && (toolbox = (HandHeldToolbox)((ContainerToolbox)player.containerMenu).base).isThisContainer(stack)) {
            toolbox.saveAsThrown(stack);
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
        return new HandHeldToolbox(player, hand, stack, 9);
    }
}

