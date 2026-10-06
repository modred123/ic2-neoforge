/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.InteractionResult
 *  net.minecraftforge.fluids.Fluid
 *  net.minecraftforge.fluids.FluidRegistry
 *  net.minecraftforge.fluids.FluidStack
 *  net.minecraftforge.fluids.capability.FluidTankProperties
 *  net.minecraftforge.fluids.capability.IFluidHandler
 *  net.minecraftforge.fluids.capability.IFluidHandlerItem
 *  net.minecraftforge.fluids.capability.IFluidTankProperties
 *  net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper
 */
package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.crop.TileEntityCrop;
import ic2.core.profile.NotExperimental;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;



@NotExperimental
public enum CellType
implements IIdProvider {
    EMPTY(0),
    water(1, Fluids.WATER),
    lava(2, Fluids.LAVA),
    air(3, FluidName.air.getInstance()),
    electrolyzed_water(4),
    biofuel(5),
    coalfuel(6),
    bio(7),
    hydrated_coal(8),
    weed_ex(9),
    hydration(10);
    private final int id;
    final Fluid fluid;
    private CellType(int id) {
        this(id, null);
    }

    private CellType(int id, Fluid fluid) {
        this.id = id;
        this.fluid = fluid;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public int getId() {
        return this.id;
    }

    public int getStackSize() {
        return this == weed_ex || this == hydration ? 1 : 64;
    }

    public boolean isFluidContainer() {
        return this.fluid != null || this == EMPTY;
    }

    public boolean hasCropAction() {
        return this == water || this == weed_ex || this == hydration;
    }

    public int getUsage(ItemStack stack) {
        switch (this) {
            case weed_ex: {
                return StackUtil.getTag(stack) != null ? StackUtil.getTag(stack).getInt("weedEX") : 0;
            }
            case hydration: {
                return StackUtil.getTag(stack) != null ? StackUtil.getTag(stack).getInt("hydration") : 0;
            }
        }
        return 0;
    }

    public int getMaximum(ItemStack stack) {
        switch (this) {
            case weed_ex: {
                return 64;
            }
            case hydration: {
                return 10000;
            }
        }
        return 0;
    }

    public InteractionResult doCropAction(ItemStack stack, Consumer<ItemStack> result, TileEntityCrop crop, boolean manual) {
        assert (this.hasCropAction());
        switch (this) {
            case water: {
                if (crop.getStorageWater() < 10) {
                    crop.setStorageWater(10);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            case weed_ex: {
                WeedExHandler handler = new WeedExHandler(stack);
                FluidStack drained = handler.drain(50, FluidAction.EXECUTE);
                if (!drained.isEmpty()) {
                    crop.applyWeedEx(1, false, true, false);
                    result.accept(handler.getContainer());
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            case hydration: {
                HydrationHandler handler = new HydrationHandler(stack, manual);
                FluidStack drained = handler.drain(manual ? 10000 : 180, FluidAction.EXECUTE);
                if (!drained.isEmpty()) {
                    crop.applyHydration(drained.getAmount(), false);
                    result.accept(handler.getContainer());
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }
        throw new IllegalStateException("Type was " + this);
    }


    private static class HydrationHandler
    implements IFluidHandlerItem {
        public static final String NBT = "hydration";
        public static final int CHARGES = 10000;
        protected ItemStack container;
        protected final boolean manual;

        public HydrationHandler(ItemStack stack, boolean manual) {
            this.container = stack;
            this.manual = manual;
        }

        public ItemStack getContainer() {
            return this.container;
        }

        public int getTanks() {
            return 1;
        }

        public FluidStack getFluidInTank(int tank) {
            int remaining = StackUtil.getTag(container) != null ? 10000 - StackUtil.getTag(container).getInt(NBT) : 10000;
            return new FluidStack(Fluids.WATER, remaining);
        }

        public int getTankCapacity(int tank) {
            return 10000;
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            return stack != null && stack.getFluid() == Fluids.WATER;
        }

        public int fill(FluidStack resource, FluidAction doFill) {
            return 0;
        }

        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            if (resource == null || resource.getFluid() != Fluids.WATER) {
                return FluidStack.EMPTY;
            }
            return this.drain(resource.getAmount(), doDrain);
        }

        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            int remaining = StackUtil.getTag(container) != null ? 10000 - StackUtil.getTag(container).getInt(NBT) : 10000;
            int target = Math.min(maxDrain, remaining);
            if (!this.manual && target > 180) {
                target = 180;
            }
            if (doDrain.execute()) {
                CompoundTag nbt = StackUtil.getOrCreateNbtData(this.container);
                int amount = nbt.getInt(NBT) + target;
                if (amount >= 10000) {
                    this.container = StackUtil.decSize(this.container);
                } else {
                    nbt.putInt(NBT, amount);
                }
            }
            return new FluidStack(Fluids.WATER, target);
        }
    }

    private static class WeedExHandler
    implements IFluidHandlerItem {
        public static final String NBT = "weedEX";
        public static final int CHARGES = 64;
        private static final int DRAIN = 50;
        protected ItemStack container;

        public WeedExHandler(ItemStack stack) {
            this.container = stack;
        }

        public ItemStack getContainer() {
            return this.container;
        }

        public int getTanks() {
            return 1;
        }

        public FluidStack getFluidInTank(int tank) {
            int remaining = StackUtil.getTag(container) != null ? 64 - StackUtil.getTag(container).getInt(NBT) : 64;
            return new FluidStack(FluidName.weed_ex.getInstance(), remaining * 50);
        }

        public int getTankCapacity(int tank) {
            return 3200;
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            return stack != null && stack.getFluid() == FluidName.weed_ex.getInstance();
        }

        public int fill(FluidStack resource, FluidAction doFill) {
            return 0;
        }

        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            if (resource == null || resource.getFluid() != FluidName.weed_ex.getInstance()) {
                return FluidStack.EMPTY;
            }
            return this.drain(resource.getAmount(), doDrain);
        }

        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            if (maxDrain < 50) {
                return FluidStack.EMPTY;
            }
            if (doDrain.execute()) {
                CompoundTag nbt = StackUtil.getOrCreateNbtData(this.container);
                int amount = nbt.getInt(NBT) + 1;
                if (amount >= 64) {
                    this.container = StackUtil.decSize(this.container);
                } else {
                    nbt.putInt(NBT, amount);
                }
            }
            return new FluidStack(FluidName.weed_ex.getInstance(), 50);
        }
    }

    public static class CellFluidHandler
    implements IFluidHandlerItem {
        private static final Map<Fluid, CellType> VALID_FLUIDS = new IdentityHashMap(Arrays.stream(CellType.values()).filter(type -> type.fluid != null).collect(Collectors.toMap(type -> type.fluid, Function.identity())));
        protected ItemStack container;
        protected final Function<ItemStack, CellType> typeGetter;

        public CellFluidHandler(ItemStack container, Function<ItemStack, CellType> typeGetter) {
            this.container = container;
            this.typeGetter = typeGetter;
        }

        public ItemStack getContainer() {
            return this.container;
        }

        private CellType getType() {
            return this.typeGetter.apply(this.container);
        }

        public int getTanks() {
            return 1;
        }

        public FluidStack getFluidInTank(int tank) {
            CellType type = this.getType();
            assert (type.isFluidContainer());
            return type != null && type.fluid != null ? new FluidStack(type.fluid, 1000) : FluidStack.EMPTY;
        }

        public int getTankCapacity(int tank) {
            return 1000;
        }

        public boolean isFluidValid(int tank, FluidStack stack) {
            return this.canFillFluidType(stack);
        }

        public boolean canFillFluidType(FluidStack fluid) {
            assert (fluid != null);
            assert (fluid.getFluid() != null);
            return this.getType() == EMPTY && VALID_FLUIDS.containsKey(fluid.getFluid());
        }

        protected void setFluid(FluidStack stack) {
            if (stack == null || stack.isEmpty()) {
                assert (this.getType() != EMPTY);
                this.container = ItemName.cell.getItemStack(EMPTY);
            } else {
                assert (this.getType() == EMPTY);
                assert (VALID_FLUIDS.containsKey(stack.getFluid()));
                this.container = ItemName.cell.getItemStack((Enum)VALID_FLUIDS.get(stack.getFluid()));
            }
        }

        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || !this.canFillFluidType(resource)) {
                return 0;
            }
            int filled = Math.min(resource.getAmount(), 1000);
            if (action.execute()) {
                this.setFluid(new FluidStack(resource.getFluid(), filled));
            }
            return filled;
        }

        public FluidStack drain(FluidStack resource, FluidAction action) {
            CellType type = this.getType();
            if (resource == null || type.fluid == null || resource.getFluid() != type.fluid) {
                return FluidStack.EMPTY;
            }
            int drained = Math.min(resource.getAmount(), 1000);
            if (action.execute()) {
                this.setFluid(null);
            }
            return new FluidStack(type.fluid, drained);
        }

        public FluidStack drain(int maxDrain, FluidAction action) {
            CellType type = this.getType();
            if (type == null || type.fluid == null) {
                return FluidStack.EMPTY;
            }
            int drained = Math.min(maxDrain, 1000);
            if (action.execute()) {
                this.setFluid(null);
            }
            return new FluidStack(type.fluid, drained);
        }
    }
}
