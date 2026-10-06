/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.block.SoundType
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.util.InteractionResult
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.fluids.FluidRegistry
 */
package ic2.core.item;

import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.Ic2Potion;
import ic2.core.ref.Ic2Blocks;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.item.ItemMulti;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.item.type.CellType;
import ic2.core.item.type.IRadioactiveItemType;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

public class ItemHandlers {
    public static ItemMulti.IItemRightClickHandler cfPowderApply = new ItemMulti.IItemRightClickHandler(){

        @Override
        public InteractionResultHolder<ItemStack> onRightClick(ItemStack stack, Player player, InteractionHand hand) {
            net.minecraft.world.phys.HitResult position = Util.traceBlocks(player, true);
            if (position == null) {
                return InteractionResultHolder.pass(stack);
            }
            if (position instanceof net.minecraft.world.phys.BlockHitResult) {
                Level world = player.level();
                if (!world.mayInteract(player, ((net.minecraft.world.phys.BlockHitResult)position).getBlockPos())) {
                    return InteractionResultHolder.fail(stack);
                }
                if (world.getBlockState(((net.minecraft.world.phys.BlockHitResult)position).getBlockPos()).getBlock() == Blocks.WATER) {
                    stack = StackUtil.decSize(stack);
                    world.setBlockAndUpdate(((net.minecraft.world.phys.BlockHitResult)position).getBlockPos(), Ic2Blocks.FOAM.defaultBlockState());
                    return InteractionResultHolder.success(stack);
                }
            }
            return InteractionResultHolder.fail(stack);
        }
    };
    public static ItemMulti.IItemRightClickHandler scrapBoxUnpack = new ItemMulti.IItemRightClickHandler(){

        @Override
        public InteractionResultHolder<ItemStack> onRightClick(ItemStack stack, Player player, InteractionHand hand) {
            ItemStack drop;
            if (!player.level().isClientSide && (drop = Recipes.scrapboxDrops.getDrop(stack, false)) != null && player.drop(drop, false) != null && !player.getAbilities().instabuild) {
                stack = StackUtil.decSize(stack);
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
    };
    public static ItemMulti.IItemUseHandler resinUse = new ItemMulti.IItemUseHandler(){

        @Override
        public InteractionResult onUse(ItemStack stack, Player player, BlockPos pos, InteractionHand hand, Direction side) {
            Level world = player.level();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.PISTON && state.getValue(BlockStateProperties.FACING) == side) {
                BlockState newState = Blocks.STICKY_PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, side).setValue(BlockStateProperties.EXTENDED, state.getValue(BlockStateProperties.EXTENDED));
                world.setBlock(pos, newState, 3);
                if (!player.getAbilities().instabuild) {
                    StackUtil.consumeOrError(player, hand, 1);
                }
                return InteractionResult.SUCCESS;
            }
            if (side != Direction.UP) {
                return InteractionResult.PASS;
            }
            pos = pos.above();
            if (!state.isAir() || !Ic2Blocks.RESIN_SHEET.defaultBlockState().canSurvive(world, pos)) {
                return InteractionResult.PASS;
            }
            world.setBlockAndUpdate(pos, Ic2Blocks.RESIN_SHEET.defaultBlockState());
            if (!player.getAbilities().instabuild) {
                StackUtil.consumeOrError(player, hand, 1);
            }
            return InteractionResult.PASS;
        }
    };
    public static ItemMulti.IItemUpdateHandler radioactiveUpdate = new ItemMulti.IItemUpdateHandler(){

        @Override
        public void onUpdate(ItemStack stack, Level world, Entity rawEntity, int slotIndex, boolean isCurrentItem) {
            Item item = stack.getItem();
            if (item == null || !(item instanceof ItemMulti)) {
                return;
            }
            Object rawType = ((ItemMulti)item).getType(stack);
            if (!(rawType instanceof IRadioactiveItemType)) {
                return;
            }
            IRadioactiveItemType type = (IRadioactiveItemType)rawType;
            if (!(rawEntity instanceof LivingEntity)) {
                return;
            }
            LivingEntity entity = (LivingEntity)rawEntity;
            if (ItemArmorHazmat.hasCompleteHazmat(entity)) {
                return;
            }
            Ic2Potion.radiation.applyTo(entity, type.getRadiationDuration() * 20, type.getRadiationAmplifier());
        }
    };
    
    public static ItemMulti.IItemRightClickHandler openAdvancedUpgradeGUI = new ItemMulti.IItemRightClickHandler(){

        @Override
        public InteractionResultHolder<ItemStack> onRightClick(ItemStack stack, Player player, InteractionHand hand) {
            assert (stack.getItem() == ItemName.upgrade.getInstance());
            if (IC2.platform.isSimulating()) {
                IC2.platform.launchGui(player, ((ItemUpgradeModule)stack.getItem()).getInventory(player, hand, stack));
            }
            return InteractionResultHolder.success(stack);
        }
    };
    public static ItemMulti.IItemUseHandler emptyCellFill = new ItemMulti.IItemUseHandler(){

        @Override
        public InteractionResult onUse(ItemStack stack, Player player, BlockPos pos, InteractionHand hand, Direction side) {
            assert (stack.getItem() == ItemName.cell.getInstance());
            Level world = player.level();
            net.minecraft.world.phys.HitResult position = Util.traceBlocks(player, true);
            if (position == null) {
                return InteractionResult.FAIL;
            }
            if (position instanceof net.minecraft.world.phys.BlockHitResult) {
                pos = ((net.minecraft.world.phys.BlockHitResult)position).getBlockPos();
                if (!world.mayInteract(player, pos)) {
                    return InteractionResult.FAIL;
                }
                if (!world.mayInteract(player, pos)) {
                    return InteractionResult.FAIL;
                }
                LiquidUtil.LiquidData data = LiquidUtil.getLiquid(world, pos);
                if (data != null && data.isSource) {
                    if (data.liquid == Fluids.WATER && StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.water), player, true)) {
                        world.removeBlock(pos, false);
                        StackUtil.consumeOrError(player, hand, 1);
                        StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.water), player, false);
                        return InteractionResult.SUCCESS;
                    }
                    if (data.liquid == Fluids.LAVA && StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.lava), player, true)) {
                        world.removeBlock(pos, false);
                        StackUtil.consumeOrError(player, hand, 1);
                        StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.lava), player, false);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.PASS;
        }
    };

    public static ItemMulti.IItemUseHandler getFluidPlacer(final Block type) {
        return new ItemMulti.IItemUseHandler(){

            @Override
            public InteractionResult onUse(ItemStack stack, Player player, BlockPos pos, InteractionHand hand, Direction side) {
                assert (stack.getItem() == ItemName.misc_resource.getInstance());
                Level world = player.level();
                if (!world.getBlockState(pos).canBeReplaced()) {
                    pos = pos.relative(side);
                }
                if (player.canInteractWithBlock(pos, 0.5) && world.getBlockState(pos).canBeReplaced()) {
                    world.setBlockAndUpdate(pos, type.defaultBlockState());
                    SoundType sound = placedStateSound(type.defaultBlockState(), world, pos);
                    world.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f);
                    StackUtil.consumeOrError(player, hand, 1);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        };
    }

    private static SoundType placedStateSound(BlockState state, Level world, BlockPos pos) {
        return state.getSoundType(world, pos, null);
    }
}

