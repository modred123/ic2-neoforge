/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.item.Rarity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item.tool;
import net.neoforged.api.distmarker.OnlyIn;

import ic2.api.crops.CropCard;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.crop.TileEntityCrop;
import ic2.core.init.Localization;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.tool.ContainerCropnalyzer;
import ic2.core.item.tool.HandHeldCropnalyzer;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;

public class ItemCropnalyzer
extends BaseElectricItem
implements IHandHeldInventory {
    public ItemCropnalyzer() {
        super(ItemName.cropnalyzer, 100000.0, 128.0, 2);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (IC2.platform.isSimulating()) {
            IC2.platform.launchGui(player, this.getInventory(player, hand, stack));
        }
        return InteractionResultHolder.success(stack);
    }

    @OnlyIn(Dist.CLIENT)
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public IHasGui getInventory(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        return new HandHeldCropnalyzer(player, hand, stack);
    }

    public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        HandHeldCropnalyzer cropnalyzer;
        if (player instanceof ServerPlayer && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerCropnalyzer && (cropnalyzer = (HandHeldCropnalyzer)((ContainerCropnalyzer)player.containerMenu).base).isThisContainer(stack)) {
            cropnalyzer.saveAsThrown(stack);
            ((ServerPlayer)player).closeContainer();
        }
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        if (player == null || world.isClientSide || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityCrop) {
            TileEntityCrop crop = (TileEntityCrop)te;
            if (crop.getCrop() == null) {
                return InteractionResult.PASS;
            }
            if (ElectricItem.manager.discharge(StackUtil.get(player, hand), HandHeldCropnalyzer.energyForLevel(2), 3, true, false, false) > 0.0) {
                CropCard plant = crop.getCrop();
                IC2.platform.messagePlayer(player, "Crop name: " + Localization.translate(plant.getUnlocalizedName()) + " (by " + plant.getDiscoveredBy() + ')', new Object[0]);
                IC2.platform.messagePlayer(player, "Crop size: " + crop.getCurrentAge() + '/' + plant.getMaxAge(), new Object[0]);
                IC2.platform.messagePlayer(player, "Nutrient storage: " + crop.getStorageNutrients() + "/100", new Object[0]);
                IC2.platform.messagePlayer(player, "Water storage: " + crop.getStorageWater() + "/200", new Object[0]);
                IC2.platform.messagePlayer(player, "Weed-Ex storage: " + crop.getStorageWeedEX() + "/100", new Object[0]);
                IC2.platform.messagePlayer(player, "Growth points: " + crop.getGrowthPoints() + '/' + plant.getGrowthDuration(crop), new Object[0]);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}

