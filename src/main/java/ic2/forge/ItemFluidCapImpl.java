package ic2.forge;

import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import ic2.forge.EnvFluidHandlerForge;
import ic2.forge.Ic2FluidStackImpl;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.apache.commons.lang3.mutable.Mutable;

final class ItemFluidCapImpl implements IFluidHandlerItem, Mutable<ItemStack> {
    private ItemStack stack;

    public ItemFluidCapImpl(ItemStack itemStack) {
        this.stack = itemStack;
    }

    public int getTanks() {
        return 1;
    }

    public int getTankCapacity(int n) {
        if (n != 0) {
            return 0;
        }
        Ic2FluidItem ic2FluidItem = (Ic2FluidItem)this.stack.getItem();
        return ic2FluidItem.getCapacityMb(this.stack);
    }

    public FluidStack getFluidInTank(int n) {
        if (n != 0) {
            return FluidStack.EMPTY;
        }
        Ic2FluidItem ic2FluidItem = (Ic2FluidItem)this.stack.getItem();
        Ic2FluidStack ic2FluidStack = ic2FluidItem.drainMb(this.stack, Integer.MAX_VALUE, true, null);
        return EnvFluidHandlerForge.getForgeFs(ic2FluidStack);
    }

    public boolean isFluidValid(int n, FluidStack fluidStack) {
        return n == 0;
    }

    public FluidStack drain(int n, IFluidHandler.FluidAction fluidAction) {
        if (n <= 0 || this.stack.getCount() != 1) {
            return FluidStack.EMPTY;
        }
        Ic2FluidItem ic2FluidItem = (Ic2FluidItem)this.stack.getItem();
        return EnvFluidHandlerForge.getForgeFs(ic2FluidItem.drainMb(this.stack, n, fluidAction.simulate(), (Mutable<ItemStack>)this));
    }

    public FluidStack drain(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
        if (fluidStack == null || fluidStack.isEmpty() || this.stack.getCount() != 1) {
            return FluidStack.EMPTY;
        }
        Ic2FluidItem ic2FluidItem = (Ic2FluidItem)this.stack.getItem();
        int n = ic2FluidItem.drainMb(this.stack, new Ic2FluidStackImpl(fluidStack), fluidAction.simulate(), (Mutable<ItemStack>)this);
        if (n <= 0) {
            return FluidStack.EMPTY;
        }
        fluidStack = fluidStack.copy();
        fluidStack.setAmount(n);
        return fluidStack;
    }

    public int fill(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
        if (fluidStack == null || fluidStack.isEmpty() || this.stack.getCount() != 1) {
            return 0;
        }
        Ic2FluidItem ic2FluidItem = (Ic2FluidItem)this.stack.getItem();
        return ic2FluidItem.fillMb(this.stack, new Ic2FluidStackImpl(fluidStack), fluidAction.simulate(), this);
    }

    public ItemStack getContainer() {
        return this.stack;
    }

    public ItemStack getValue() {
        return this.stack;
    }

    public void setValue(ItemStack itemStack) {
        this.stack = itemStack;
    }
}
