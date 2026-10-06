package ic2.forge;

import ic2.core.fluid.FluidTankInfo;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.forge.EnvFluidHandlerForge;
import ic2.forge.Ic2FluidStackImpl;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

final class BlockFluidCapImpl {
    private final Ic2FluidBlock parent;
    private final BlockEntity be;

    public BlockFluidCapImpl(Ic2FluidBlock ic2FluidBlock, BlockEntity blockEntity) {
        this.parent = ic2FluidBlock;
        this.be = blockEntity;
    }

    public IFluidHandler getHandler(Direction side) {
        return side != null ? new SideHandler(side) : null;
    }

    private class SideHandler implements IFluidHandler {
        private final Direction side;

        SideHandler(Direction direction) {
            this.side = direction;
        }

        public int getTanks() {
            FluidTankInfo[] fluidTankInfoArray = BlockFluidCapImpl.this.parent.getTankInfos(null, null, null, BlockFluidCapImpl.this.be);
            if (fluidTankInfoArray == null) {
                return 0;
            }
            int n = 1 << this.side.ordinal();
            int n2 = 0;
            for (FluidTankInfo fluidTankInfo : fluidTankInfoArray) {
                if (((fluidTankInfo.getDrainSideMask() | fluidTankInfo.getFillSideMask()) & n) == 0) continue;
                ++n2;
            }
            return n2;
        }

        private FluidTankInfo getTankInfo(int n) {
            FluidTankInfo[] fluidTankInfoArray = BlockFluidCapImpl.this.parent.getTankInfos(null, null, null, BlockFluidCapImpl.this.be);
            if (fluidTankInfoArray == null) {
                return null;
            }
            int n2 = 1 << this.side.ordinal();
            for (FluidTankInfo fluidTankInfo : fluidTankInfoArray) {
                if (((fluidTankInfo.getDrainSideMask() | fluidTankInfo.getFillSideMask()) & n2) == 0 || n-- != 0) continue;
                return fluidTankInfo;
            }
            return null;
        }

        public int getTankCapacity(int n) {
            FluidTankInfo fluidTankInfo = this.getTankInfo(n);
            return fluidTankInfo != null ? fluidTankInfo.getCapacity() : 0;
        }

        public FluidStack getFluidInTank(int n) {
            FluidTankInfo fluidTankInfo = this.getTankInfo(n);
            return fluidTankInfo != null ? EnvFluidHandlerForge.getForgeFs(fluidTankInfo.getContent()) : FluidStack.EMPTY;
        }

        public boolean isFluidValid(int n, FluidStack fluidStack) {
            FluidTankInfo fluidTankInfo = this.getTankInfo(n);
            return fluidTankInfo != null && fluidTankInfo.getCapacity() > 0;
        }

        public FluidStack drain(int n, IFluidHandler.FluidAction fluidAction) {
            if (n <= 0) {
                return FluidStack.EMPTY;
            }
            return EnvFluidHandlerForge.getForgeFs(BlockFluidCapImpl.this.parent.drainMb(null, null, null, BlockFluidCapImpl.this.be, this.side, n, fluidAction.simulate()));
        }

        public FluidStack drain(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
            if (fluidStack == null || fluidStack.isEmpty()) {
                return FluidStack.EMPTY;
            }
            int n = BlockFluidCapImpl.this.parent.drainMb(null, null, null, BlockFluidCapImpl.this.be, this.side, new Ic2FluidStackImpl(fluidStack), fluidAction.simulate());
            if (n <= 0) {
                return FluidStack.EMPTY;
            }
            fluidStack = fluidStack.copy();
            fluidStack.setAmount(n);
            return fluidStack;
        }

        public int fill(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
            if (fluidStack == null || fluidStack.isEmpty()) {
                return 0;
            }
            return BlockFluidCapImpl.this.parent.fillMb(null, null, null, BlockFluidCapImpl.this.be, this.side, new Ic2FluidStackImpl(fluidStack), fluidAction.simulate());
        }
    }
}
