/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  javax.annotation.Nullable
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.fluids.Fluid
 *  net.minecraftforge.fluids.FluidRegistry
 *  net.minecraftforge.fluids.FluidStack
 *  net.minecraftforge.fluids.FluidUtil
 *  net.minecraftforge.fluids.capability.CapabilityFluidHandler
 *  net.minecraftforge.fluids.capability.IFluidHandlerItem
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.DistOnly
 */
package ic2.core.item;
import net.neoforged.api.distmarker.OnlyIn;

import com.google.common.base.Function;
import ic2.api.item.IItemHudInfo;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.item.capability.CapabilityFluidHandlerItem;
import ic2.core.ref.FluidName;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.api.distmarker.Dist;

public abstract class ItemIC2FluidContainer
extends ItemIC2
implements IMultiItem<FluidName>,
IItemHudInfo {
    protected final int capacity;

    public ItemIC2FluidContainer(ItemName name, int capacity) {
        super(name);
        this.capacity = capacity;
        this.setHasSubtypes(true);
    }

    /** 1.21.1 能力注册（由 FmlMod.registerCapabilities 调用） */
    public void registerFluidCapabilities(RegisterCapabilitiesEvent event) {
        final int cap = this.capacity;
        final ItemIC2FluidContainer self = this;
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new CapabilityFluidHandlerItem(stack, cap){
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return fluid != null && self.canfill(fluid.getFluid());
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluid) {
                return fluid != null && self.canfill(fluid.getFluid());
            }
        }, self);
    }

    @Override
    public ItemStack getItemStack(FluidName type) {
        return this.getItemStack(type.getInstance());
    }

    public ItemStack getItemStack(Fluid fluid) {
        ItemStack ret = new ItemStack((Item)this);
        if (fluid == null) {
            return ret;
        }
        IFluidHandlerItem handler = FluidUtil.getFluidHandler((ItemStack)ret).orElse(null);
        if (handler == null) {
            return null;
        }
        if (handler.fill(new FluidStack(fluid, Integer.MAX_VALUE), FluidAction.EXECUTE) > 0) {
            return handler.getContainer();
        }
        return null;
    }

    @Override
    public ItemStack getItemStack(String variant) {
        if (variant == null || variant.isEmpty()) {
            return new ItemStack((Item)this);
        }
        Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(net.minecraft.resources.ResourceLocation.parse(variant));
        if (fluid == null) {
            return null;
        }
        return this.getItemStack(fluid);
    }

    @Override
    public String getVariant(ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this);
        }
        FluidStack fs = FluidUtil.getFluidContained((ItemStack)stack).orElse(null);
        if (fs == null || fs.getFluid() == null) {
            return null;
        }
        return net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fs.getFluid()).getPath();
    }

    @Override
    public Set<FluidName> getAllTypes() {
        return EnumSet.allOf(FluidName.class);
    }

    @Override
    public Set<ItemStack> getAllStacks() {
        HashSet<ItemStack> ret = new HashSet<ItemStack>();
        ret.add(new ItemStack((Item)this));
        for (Fluid fluid : net.minecraft.core.registries.BuiltInRegistries.FLUID) {
            ItemStack add = this.getItemStack(fluid);
            if (add == null) continue;
            ret.add(add);
        }
        return ret;
    }

    public boolean hasContainerItem(ItemStack stack) {
        return FluidUtil.getFluidContained((ItemStack)stack).orElse(null) != null;
    }

    public ItemStack getContainerItem(ItemStack stack) {
        if (!this.hasContainerItem(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack ret = StackUtil.copyWithSize(stack, 1);
        IFluidHandlerItem handler = FluidUtil.getFluidHandler((ItemStack)ret).orElse(null);
        handler.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
        return handler.getContainer();
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
        super.appendHoverText(stack, context, tooltip, advanced);
        FluidStack fs = FluidUtil.getFluidContained((ItemStack)stack).orElse(null);
        if (fs != null) {
            tooltip.add(Component.literal("< " + fs.getFluid().getFluidType().getDescriptionId() + ", " + fs.getAmount() + " mB >"));
        } else {
            tooltip.add(Component.literal(Localization.translate("ic2.item.FluidContainer.Empty")));
        }
    }

    @Override
    public List<String> getHudInfo(ItemStack stack, boolean advanced) {
        LinkedList<String> info = new LinkedList<String>();
        FluidStack fs = FluidUtil.getFluidContained((ItemStack)stack).orElse(null);
        if (fs != null) {
            info.add("< " + fs.getFluid().getFluidType().getDescriptionId() + ", " + fs.getAmount() + " mB >");
        } else {
            info.add(Localization.translate("ic2.item.FluidContainer.Empty"));
        }
        return info;
    }

    public abstract boolean canfill(Fluid var1);
}

