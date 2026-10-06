/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  org.apache.commons.lang3.mutable.Mutable
 */
package ic2.core.item;

import ic2.core.crop.TileEntityCrop;
import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.item.Ic2BucketItem;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.Mutable;

public class ItemClassicCell
extends Ic2BucketItem
implements Ic2FluidItem {
    private static final int WEED_EX_DRAIN = 50;
    private static Map<Fluid, ItemClassicCell> instances = new IdentityHashMap<Fluid, ItemClassicCell>();
    private final Fluid fluid;
    private final int charges;

    public ItemClassicCell(Item.Properties properties, Fluid fluid, int n) {
        super(fluid, properties);
        this.fluid = fluid;
        this.charges = n;
        if (fluid != null) {
            instances.put(fluid, this);
        }
    }

    @Override
    public Item getEmptiedBucketItem() {
        return Ic2Items.EMPTY_CELL;
    }

    @Override
    public List<Fluid> getDrainableFluidList() {
        return List.of(Fluids.LAVA, Fluids.WATER);
    }

    @Override
    public Item getBucketItem(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return Ic2Items.WATER_CELL;
        }
        if (fluid == Fluids.LAVA) {
            return Ic2Items.LAVA_CELL;
        }
        return Ic2Items.EMPTY_CELL;
    }

    @Override
    public boolean bucketUseOnBlock(UseOnContext useOnContext) {
        BlockEntity blockEntity;
        return (this == Ic2Items.WATER_CELL || this == Ic2Items.WEED_EX_CELL || this == Ic2Items.HYDRATION_CELL) && (blockEntity = useOnContext.getLevel().getBlockEntity(useOnContext.getClickedPos())) instanceof TileEntityCrop && this.useOnCrop(useOnContext.getItemInHand(), (TileEntityCrop)blockEntity, true);
    }

    public boolean useOnCrop(ItemStack itemStack, TileEntityCrop tileEntityCrop, boolean bl) {
        if (this == Ic2Items.WATER_CELL) {
            if (tileEntityCrop.getStorageWater() < 10) {
                tileEntityCrop.setStorageWater(10);
                return true;
            }
        } else if (this == Ic2Items.WEED_EX_CELL) {
            if (tileEntityCrop.applyWeedEx(50, true, bl, false) > 0) {
                int n = this.getUsage(itemStack) + 1;
                if (n >= this.charges) {
                    itemStack = StackUtil.decSize(itemStack);
                } else {
                    this.setUsage(itemStack, n);
                }
                return true;
            }
        } else if (this == Ic2Items.HYDRATION_CELL) {
            int n = this.getUsage(itemStack) + 1;
            int n2 = Math.max(0, this.charges - n);
            if (!bl && n2 > 180) {
                n2 = 180;
            }
            if ((n2 = tileEntityCrop.applyHydration(n2, false)) > 0) {
                if ((n += n2) >= this.charges) {
                    itemStack = StackUtil.decSize(itemStack);
                } else {
                    this.setUsage(itemStack, n);
                }
                return true;
            }
        }
        return false;
    }

    private int getUsage(ItemStack itemStack) {
        if (this.charges <= 1) {
            return 0;
        }
        CompoundTag compoundTag = StackUtil.getTag(itemStack);
        return compoundTag != null ? compoundTag.getInt("uses") : 0;
    }

    private void setUsage(ItemStack itemStack, int n) {
        if (n <= 0) {
            itemStack.remove(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        } else {
            StackUtil.getOrCreateNbtData(itemStack).putInt("uses", n);
        }
    }

    private double getChargeLevel(ItemStack itemStack) {
        return (double)(this.charges - this.getUsage(itemStack)) / (double)this.charges;
    }

    public boolean isBarVisible(ItemStack itemStack) {
        return this.getUsage(itemStack) > 0;
    }

    public int getBarWidth(ItemStack itemStack) {
        return (int)Math.round(this.getChargeLevel(itemStack) * 13.0);
    }

    public int getBarColor(ItemStack itemStack) {
        return Mth.hsvToRgb((float)((float)(this.getChargeLevel(itemStack) / 3.0)), (float)1.0f, (float)1.0f);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (this.charges > 1 && itemStack.getCount() == 1 && tooltipFlag.isAdvanced()) {
            list.add((Component)Component.translatable((String)"item.durability", (Object[])new Object[]{this.charges - this.getUsage(itemStack), this.charges}).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Ic2FluidStack getFluidStack(ItemStack itemStack) {
        if (this.fluid == Fluids.EMPTY) {
            return Ic2FluidStack.EMPTY;
        }
        if (this.fluid != null) {
            return Ic2FluidStack.create(this.fluid, 1000);
        }
        return null;
    }

    @Override
    public int getCapacityMb(ItemStack itemStack) {
        return this.fluid != null ? 1000 : 0;
    }

    @Override
    public Ic2FluidStack drainMb(ItemStack itemStack, int n, boolean bl, Mutable<ItemStack> mutable) {
        if (mutable != null) {
            mutable.setValue(itemStack);
        }
        if (itemStack.getCount() != 1) {
            throw new IllegalArgumentException("invalid stack size");
        }
        if (this.fluid == Fluids.EMPTY) {
            return Ic2FluidStack.EMPTY;
        }
        if (this.fluid == null) {
            return null;
        }
        if (n < 1000) {
            return Ic2FluidStack.EMPTY;
        }
        if (!bl) {
            itemStack.shrink(1);
        }
        if (mutable != null) {
            mutable.setValue(new ItemStack(Ic2Items.EMPTY_CELL));
        }
        return Ic2FluidStack.create(this.fluid, 1000);
    }

    @Override
    public int drainMb(ItemStack itemStack, Ic2FluidStack ic2FluidStack, boolean bl, Mutable<ItemStack> mutable) {
        if (mutable != null) {
            mutable.setValue(itemStack);
        }
        if (itemStack.getCount() != 1) {
            throw new IllegalArgumentException("invalid stack size");
        }
        if (ic2FluidStack.getAmountMb() < 1000) {
            return 0;
        }
        if (this.fluid == null || this.fluid == Fluids.EMPTY || !ic2FluidStack.hasExactFluid(this.fluid)) {
            return 0;
        }
        if (!bl) {
            itemStack.shrink(1);
        }
        if (mutable != null) {
            mutable.setValue(new ItemStack(Ic2Items.EMPTY_CELL));
        }
        return 1000;
    }

    @Override
    public int fillMb(ItemStack itemStack, Ic2FluidStack ic2FluidStack, boolean bl, Mutable<ItemStack> mutable) {
        if (mutable != null) {
            mutable.setValue(itemStack);
        }
        if (itemStack.getCount() != 1) {
            throw new IllegalArgumentException("invalid stack size");
        }
        if (ic2FluidStack.getAmountMb() < 1000) {
            return 0;
        }
        if (this.fluid != Fluids.EMPTY) {
            return 0;
        }
        ItemClassicCell itemClassicCell = instances.get(ic2FluidStack.getFluid());
        if (itemClassicCell == null || !ic2FluidStack.hasExactFluid(itemClassicCell.fluid)) {
            return 0;
        }
        if (!bl) {
            itemStack.shrink(1);
        }
        if (mutable != null) {
            mutable.setValue(new ItemStack(itemClassicCell));
        }
        return 1000;
    }
}

