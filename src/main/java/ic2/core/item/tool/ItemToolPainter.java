/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Registry
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.Sheep
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BannerBlock
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ConcretePowderBlock
 *  net.minecraft.world.level.block.GlazedTerracottaBlock
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.level.block.StainedGlassBlock
 *  net.minecraft.world.level.block.StainedGlassPaneBlock
 *  net.minecraft.world.level.block.WallBannerBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.item.tool.ItemToolCrafting;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.Ic2Color;
import ic2.core.util.StackUtil;
import ic2.core.util.VanillaColorBlockId;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemToolPainter
extends ItemToolCrafting
implements IBoxable {
    Ic2Color color = null;
    private static final int maxDamage = 32;

    public ItemToolPainter(Item.Properties properties, Ic2Color ic2Color) {
        super(properties);
        this.color = ic2Color;
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        if (this.color == null) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        Player player = useOnContext.getPlayer();
        InteractionHand interactionHand = useOnContext.getHand();
        if (!(itemStack.getItem() instanceof ItemToolPainter)) {
            return InteractionResult.PASS;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (this.colorBlock(level, blockPos, block, blockState, this.color)) {
            this.damagePainter(itemStack, player, interactionHand, this.color);
            if (level.isClientSide && player != null) {
                player.playSound(Ic2SoundEvents.ITEM_PAINTER_USE, 1.0f, 1.0f);
            }
            return level.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean colorBlock(Level level, BlockPos blockPos, Block block, BlockState blockState, Ic2Color ic2Color) {
        DyeColor dyeColor = ic2Color.dyeColor;
        for (Property property2 : blockState.getValues().keySet()) {
            if (property2.getValueClass() != DyeColor.class) continue;
            Property property3 = property2;
            DyeColor dyeColor2 = (DyeColor)blockState.getValue(property3);
            if (dyeColor2 == dyeColor || !property3.getPossibleValues().contains(dyeColor)) {
                return false;
            }
            level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(property3, dyeColor));
            return true;
        }
        if (!ItemToolPainter.canColor(block, ic2Color.dyeColor)) {
            return false;
        }
        List list = block.defaultBlockState().getTags().toList();
        if (list.contains(BlockTags.WOOL)) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getColorBlockState(ic2Color.dyeColor, VanillaColorBlockId.WOOL));
            return true;
        }
        if (block instanceof StainedGlassBlock || block.defaultBlockState().is(Blocks.GLASS)) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getColorBlockState(ic2Color.dyeColor, VanillaColorBlockId.STAINED_GLASS));
            return true;
        }
        if (block instanceof StainedGlassPaneBlock || block.defaultBlockState().is(Blocks.GLASS_PANE)) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getBlockStateWithProperties(ic2Color.dyeColor, VanillaColorBlockId.STAINED_GLASS_PANE, blockState));
            return true;
        }
        if (list.contains(BlockTags.BEDS)) {
            BedBlock bedBlock = (BedBlock)block;
            BlockPos blockPos2 = blockPos.relative(bedBlock.getConnectedDirection(blockState));
            BlockState blockState2 = level.getBlockState(blockPos2);
            if (blockState2.is(bedBlock)) {
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 48);
                level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 48);
                level.setBlockAndUpdate(blockPos, ItemToolPainter.getBlockStateWithProperties(ic2Color.dyeColor, VanillaColorBlockId.BED, blockState));
                level.setBlockAndUpdate(blockPos2, ItemToolPainter.getBlockStateWithProperties(ic2Color.dyeColor, VanillaColorBlockId.BED, blockState2));
            }
            return true;
        }
        if (list.contains(BlockTags.CANDLES)) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getBlockStateWithProperties(ic2Color.dyeColor, VanillaColorBlockId.CANDLE, blockState));
            return true;
        }
        if (block instanceof BannerBlock) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getBlockStateWithProperties(ic2Color.dyeColor, VanillaColorBlockId.BANNER, blockState));
            return true;
        }
        if (block instanceof WallBannerBlock) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getBlockStateWithProperties(ic2Color.dyeColor, VanillaColorBlockId.WALL_BANNER, blockState));
            return true;
        }
        if (list.contains(BlockTags.TERRACOTTA)) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getColorBlockState(ic2Color.dyeColor, VanillaColorBlockId.TERRACOTTA));
            return true;
        }
        if (block instanceof GlazedTerracottaBlock) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getBlockStateWithProperties(ic2Color.dyeColor, VanillaColorBlockId.GLAZED_TERRACOTTA, blockState));
            return true;
        }
        if (block instanceof ConcretePowderBlock) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getColorBlockState(ic2Color.dyeColor, VanillaColorBlockId.CONCRETE_POWDER));
            return true;
        }
        if (list.contains(BlockTags.WOOL_CARPETS)) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getColorBlockState(ic2Color.dyeColor, VanillaColorBlockId.CARPET));
            return true;
        }
        if (list.contains(BlockTags.SHULKER_BOXES)) {
            BlockEntity oldBlockEntity = level.getBlockEntity(blockPos);
            if (oldBlockEntity == null) {
                return false;
            }
            CompoundTag compoundTag = oldBlockEntity.saveWithId(level.registryAccess());
            BlockState blockState3 = ShulkerBoxBlock.getBlockByColor(ic2Color.dyeColor).withPropertiesOf(blockState);
            level.setBlockAndUpdate(blockPos, blockState3);
            BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, blockState3, compoundTag, level.registryAccess());
            level.setBlockEntity(blockEntity);
            return true;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId.getNamespace().equals("minecraft") && blockId.getPath().contains("concrete")) {
            level.setBlockAndUpdate(blockPos, ItemToolPainter.getColorBlockState(ic2Color.dyeColor, VanillaColorBlockId.CONCRETE));
            return true;
        }
        return false;
    }

    public static boolean canColor(Block block, DyeColor dyeColor) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        return !resourceLocation.getPath().contains(dyeColor.getName());
    }

    public static BlockState getColorBlockState(DyeColor dyeColor, VanillaColorBlockId vanillaColorBlockId) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("minecraft", dyeColor.getName() + "_" + vanillaColorBlockId.id);
        return BuiltInRegistries.BLOCK.get(resourceLocation).defaultBlockState();
    }

    public static BlockState getBlockStateWithProperties(DyeColor dyeColor, VanillaColorBlockId vanillaColorBlockId, BlockState blockState) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("minecraft", dyeColor.getName() + "_" + vanillaColorBlockId.id);
        return BuiltInRegistries.BLOCK.get(resourceLocation).withPropertiesOf(blockState);
    }

    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        Sheep sheep;
        if (this.color == null) {
            return InteractionResult.PASS;
        }
        if (livingEntity instanceof Sheep && (sheep = (Sheep)livingEntity).getColor() != this.color.dyeColor) {
            sheep.setColor(this.color.dyeColor);
            this.damagePainter(itemStack, player, player.getUsedItemHand(), this.color);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = StackUtil.get(player, interactionHand);
        if (!level.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player)) {
            CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
            boolean bl = !compoundTag.getBoolean("autoRefill");
            compoundTag.putBoolean("autoRefill", bl);
            if (bl) {
                IC2.sideProxy.messagePlayer(player, "Painter automatic refill mode enabled", new Object[0]);
            } else {
                IC2.sideProxy.messagePlayer(player, "Painter automatic refill mode disabled", new Object[0]);
            }
            return new InteractionResultHolder(InteractionResult.SUCCESS, (Object)itemStack);
        }
        return new InteractionResultHolder(InteractionResult.PASS, (Object)itemStack);
    }

    public void damagePainter(ItemStack itemStack, Player player, InteractionHand interactionHand, Ic2Color ic2Color) {
        assert (ic2Color != null);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
            CompoundTag compoundTag = StackUtil.getOrCreateNbtData(itemStack);
            if (compoundTag.getBoolean("autoRefill") && StackUtil.consumeFromPlayerInventory(player, StackUtil.sameItem(Ic2Items.PAINTER), 1, false)) {
                player.setItemInHand(interactionHand, new ItemStack((ItemLike)itemStack.getItem(), 1));
            } else {
                player.setItemInHand(interactionHand, new ItemStack((ItemLike)Ic2Items.PAINTER, 1));
            }
        } else {
            itemStack.hurtAndBreak(1, player, interactionHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemStack) {
        return super.canBeStoredInToolbox(itemStack);
    }
}

