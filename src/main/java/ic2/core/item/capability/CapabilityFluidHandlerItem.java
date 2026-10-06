package ic2.core.item.capability;

import ic2.core.util.StackUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * ex112 CapabilityFluidHandlerItem 1.21.1 适配版。
 * 1.21.1 的 FluidHandlerItemStack 构造需要 DataComponentType&lt;SimpleFluidContent&gt;，
 * 该数据组件不在编译期可用的 DataComponents 中，因此改为直接实现 IFluidHandlerItem，
 * 沿用 ex112 的 NBT 存储格式（"FluidName"/"Amount"），与 EnvFluidHandlerForge.readFluidStack 一致。
 */
public class CapabilityFluidHandlerItem
implements IFluidHandlerItem {
    protected ItemStack container;
    protected final int capacity;

    public CapabilityFluidHandlerItem(ItemStack container, int capacity) {
        this.container = container;
        this.capacity = capacity;
    }

    public ItemStack getContainer() {
        return this.container;
    }

    public int getTanks() {
        return 1;
    }

    public FluidStack getFluidInTank(int tank) {
        CompoundTag tag = StackUtil.getTag(this.container);
        if (tag == null) {
            return FluidStack.EMPTY;
        }
        String name = tag.getString("FluidName");
        int amount = tag.getInt("Amount");
        if (name.isEmpty() || amount <= 0) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(name)).orElse(Fluids.EMPTY);
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    public int getTankCapacity(int tank) {
        return this.capacity;
    }

    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    public boolean canDrainFluidType(FluidStack fluid) {
        return true;
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.isEmpty() || !this.canFillFluidType(resource)) {
            return 0;
        }
        FluidStack contained = this.getFluidInTank(0);
        if (!contained.isEmpty() && !contained.isFluidEqual(resource)) {
            return 0;
        }
        int space = this.capacity - contained.getAmount();
        int filled = Math.min(space, resource.getAmount());
        if (action.execute() && filled > 0) {
            this.setFluid(new FluidStack(resource.getFluid(), contained.getAmount() + filled));
        }
        return filled;
    }

    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.isEmpty() || !this.canDrainFluidType(resource)) {
            return FluidStack.EMPTY;
        }
        FluidStack contained = this.getFluidInTank(0);
        if (contained.isEmpty() || !contained.isFluidEqual(resource)) {
            return FluidStack.EMPTY;
        }
        return this.doDrain(Math.min(contained.getAmount(), resource.getAmount()), action);
    }

    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack contained = this.getFluidInTank(0);
        if (contained.isEmpty() || !this.canDrainFluidType(contained)) {
            return FluidStack.EMPTY;
        }
        return this.doDrain(Math.min(contained.getAmount(), maxDrain), action);
    }

    protected void setContainerToEmpty() {
        // 1.21.1 修复（第二十七轮）：原来只在【副本清理后为空】时才 setTag 回写，
        // 一旦 NBT 里还有其它键（例如电动物品的 "charge"），两个 remove 就落在副本上被丢弃
        // ⇒ 容器倒空后仍残留 FluidName/Amount，表现为"倒不干净"。
        // 正确模式是 copy → modify → 无条件写回（为空则整体移除组件）。
        CompoundTag tag = StackUtil.getTag(this.container);
        if (tag == null) {
            return;
        }
        tag.remove("FluidName");
        tag.remove("Amount");
        StackUtil.setTag(this.container, tag.isEmpty() ? null : tag);
    }

    private FluidStack doDrain(int amount, IFluidHandler.FluidAction action) {
        FluidStack contained = this.getFluidInTank(0);
        FluidStack result = new FluidStack(contained.getFluid(), amount);
        if (action.execute()) {
            int remaining = contained.getAmount() - amount;
            if (remaining <= 0) {
                this.setContainerToEmpty();
            } else {
                this.setFluid(new FluidStack(contained.getFluid(), remaining));
            }
        }
        return result;
    }

    private void setFluid(FluidStack fluid) {
        CompoundTag tag = StackUtil.getOrCreateNbtData(this.container);
        tag.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
        tag.putInt("Amount", fluid.getAmount());
    }
}
