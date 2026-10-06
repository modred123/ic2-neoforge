/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 */
package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.Ic2Explosion;
import ic2.core.block.comp.Fluids;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.util.BiomeUtil;
import ic2.core.util.LiquidUtil;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

@NotClassic
public class TileEntitySteamGenerator
extends TileEntityInventory
implements IHasGui,
IGuiValueProvider,
INetworkClientTileEntityEventListener {
    private static final float maxHeat = 500.0f;
    private static final float heatPerHu = 5.0E-4f;
    private static final float coolingPerMb = 0.1f;
    private static final float maxCooling = 2.0f;
    private static final int maxHuInput = 1200;
    private static final int maxCalcification = 100000;
    private static final int steamExpansion = 100;
    private static final float epsilon = 1.0E-4f;
    private int heatInput = 0;
    private int inputMB = 0;
    public final Ic2FluidTank waterTank;
    private int calcification = 0;
    private int outputMB = 0;
    private outputType outputFluid = outputType.NONE;
    private float systemHeat;
    private int pressure = 0;
    private boolean newActive = false;
    protected final Fluids fluids = this.addComponent(new Fluids(this));

    public TileEntitySteamGenerator(BlockPos blockPos, BlockState blockState) {
        super((BlockEntityType<? extends TileEntityInventory>)Ic2BlockEntities.STEAM_GENERATOR, blockPos, blockState);
        this.waterTank = this.fluids.addTankInsert("waterTank", 10000, Fluids.fluidPredicate(new Fluid[]{net.minecraft.world.level.material.Fluids.WATER, Ic2Fluids.DISTILLED_WATER.still}));
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compoundTag, registries);
        this.inputMB = compoundTag.getInt("inputmb");
        this.pressure = compoundTag.getInt("pressurevalve");
        this.systemHeat = compoundTag.getFloat("systemheat");
        this.calcification = compoundTag.getInt("calcification");
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compoundTag, registries);
        compoundTag.putInt("inputmb", this.inputMB);
        compoundTag.putInt("pressurevalve", this.pressure);
        compoundTag.putFloat("systemheat", this.systemHeat);
        compoundTag.putInt("calcification", this.calcification);
    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.systemHeat = Math.max(this.systemHeat, (float)BiomeUtil.getBiomeTemperature(this.getLevel(), this.worldPosition));
        if (this.isCalcified()) {
            if (this.getActive()) {
                this.setActive(false);
            }
        } else {
            this.newActive = this.work();
            if (this.getActive() != this.newActive) {
                this.setActive(this.newActive);
            }
        }
        if (!this.getActive()) {
            this.cooldown(0.01f);
        }
    }

    private boolean work() {
        Fluid fluid;
        int n;
        this.heatInput = this.requestHeat(1200);
        if (this.heatInput <= 0) {
            return false;
        }
        assert (this.heatInput <= 1200);
        this.outputMB = 0;
        this.outputFluid = outputType.NONE;
        if (this.waterTank.isEmpty() || this.inputMB <= 0) {
            this.heatup(this.heatInput);
            return true;
        }
        Fluid fluid2 = this.waterTank.getFluidStack().getFluid();
        boolean bl = fluid2 == Ic2Fluids.DISTILLED_WATER.still;
        int n2 = Math.min(this.inputMB, this.waterTank.getFluidAmount());
        float f = 100.0f + (float)this.pressure / 220.0f * 100.0f;
        float f2 = 100.0f + (float)this.pressure / 220.0f * 100.0f * 2.74f;
        float f3 = f2 - this.systemHeat;
        float f4 = this.heatInput;
        if (f3 > 1.0E-4f) {
            int n3 = (int)Math.ceil(f3 / 5.0E-4f);
            if (this.heatInput <= n3) {
                this.heatup(this.heatInput);
                if (this.pressure == 0 && this.systemHeat < 99.9999f) {
                    this.outputMB = n2;
                    this.outputFluid = bl ? outputType.DISTILLEDWATER : outputType.WATER;
                    int n4 = LiquidUtil.distribute(this, Ic2FluidStack.create(fluid2, n2), false);
                    if (n4 > 0) {
                        this.waterTank.drainMbUnchecked(n4, false);
                    }
                }
                return true;
            }
            this.heatup(n3);
            f4 -= (float)n3;
            f3 = f2 - this.systemHeat;
        }
        assert (this.systemHeat >= f2 - 1.0E-4f);
        assert (this.systemHeat >= 99.9999f);
        float f5 = Math.min(-f3 / 5.0E-4f, (float)(1200 - this.heatInput));
        int n5 = n = Math.min(n2, (int)((f4 + f5) / f));
        if ((f4 -= (float)n * f) < 0.0f) {
            this.cooldown(-f4 * 5.0E-4f);
            f3 = f2 - this.systemHeat;
        }
        if (f3 <= -0.1001f) {
            int n6 = Math.min(n2, (int)(-f3 / 0.1f));
            n6 = Math.min(n6, (int)Math.ceil(20.0));
            assert (n6 >= 0);
            this.cooldown((float)n6 * 0.1f);
            n5 = Math.max(n, n6);
        }
        if (f4 > 0.0f) {
            this.heatup(f4);
        }
        if (n5 <= 0) {
            return true;
        }
        if (!bl) {
            this.calcification += n5;
        }
        this.waterTank.drainMbUnchecked(n5, false);
        if (n <= 0) {
            return true;
        }
        this.outputMB = n * 100;
        if (this.systemHeat >= 373.9999f) {
            fluid = Ic2Fluids.SUPERHEATED_STEAM.still;
            this.outputFluid = outputType.SUPERHEATEDSTEAM;
        } else {
            fluid = Ic2Fluids.STEAM.still;
            this.outputFluid = outputType.STEAM;
        }
        int n7 = LiquidUtil.distribute(this, Ic2FluidStack.create(fluid, this.outputMB), false);
        int n8 = this.outputMB - n7;
        if (n8 > 0) {
            Level level = this.getLevel();
            if (level.random.nextInt(10) == 0) {
                new Ic2Explosion(level, null, this.worldPosition, 1, 1.0f, Ic2Explosion.Type.Heat).doExplosion();
            } else if (n8 >= 100) {
                this.waterTank.fillMbUnchecked(Ic2FluidStack.create(fluid2, n8 / 100), false);
            }
        }
        return true;
    }

    private void heatup(float f) {
        assert (f >= -1.0E-4f);
        this.systemHeat += f * 5.0E-4f;
        if (this.systemHeat > 500.0f) {
            Level level = this.getLevel();
            level.removeBlock(this.worldPosition, false);
            new Ic2Explosion(level, null, this.worldPosition, 10, 0.01f, Ic2Explosion.Type.Heat).doExplosion();
        }
    }

    private void cooldown(float f) {
        assert (f >= -1.0E-4f);
        this.systemHeat = Math.max(this.systemHeat - f, (float)BiomeUtil.getBiomeTemperature(this.getLevel(), this.worldPosition));
    }

    private int requestHeat(int n) {
        Level level = this.getLevel();
        int n2 = n;
        for (Direction direction : Util.ALL_DIRS) {
            IHeatSource iHeatSource;
            int n3;
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition.relative(direction));
            if (!(blockEntity instanceof IHeatSource) || (n3 = (iHeatSource = (IHeatSource)blockEntity).drawHeat(direction.getOpposite(), n2, true)) <= 0 || (n2 -= iHeatSource.drawHeat(direction.getOpposite(), n3, false)) != 0) continue;
            return n;
        }
        return n - n2;
    }

    @Override
    public void onNetworkEvent(Player player, int n) {
        if (n > 2000 || n < -2000) {
            if (n > 2000) {
                this.pressure = Math.min(this.pressure + (n - 2000), 300);
            }
            if (n < -2000) {
                this.pressure = Math.max(this.pressure + (n + 2000), 0);
            }
        } else {
            this.inputMB = Math.max(Math.min(this.inputMB + n, 1000), 0);
        }
    }

    public int gaugeLiquidScaled(int n, int n2) {
        if (n2 == 0) {
            if (this.waterTank.getFluidAmount() <= 0) {
                return 0;
            }
            return this.waterTank.getFluidAmount() * n / this.waterTank.getCapacity();
        }
        return 0;
    }

    @Override
    public ContainerBase<?> createServerScreenHandler(int n, Player player) {
        return new ContainerSteamGenerator(n, player.getInventory(), this);
    }

    @Override
    public ContainerBase<?> createClientScreenHandler(int n, Inventory inventory, GrowingBuffer growingBuffer) {
        return new ContainerSteamGenerator(n, inventory, this);
    }

    @Override
    public double getGuiValue(String string) {
        if ("heat".equals(string)) {
            return this.systemHeat == 0.0f ? 0.0 : (double)this.systemHeat / 500.0;
        }
        if ("calcification".equals(string)) {
            return this.calcification == 0 ? 0.0 : (double)this.calcification / 100000.0;
        }
        throw new IllegalArgumentException();
    }

    public int getOutputMB() {
        return this.outputMB;
    }

    public int getInputMB() {
        return this.inputMB;
    }

    public int getHeatInput() {
        return this.heatInput;
    }

    public int getPressure() {
        return this.pressure;
    }

    public float getSystemHeat() {
        return (float)Math.round(this.systemHeat * 10.0f) / 10.0f;
    }

    public float getCalcification() {
        return (float)Math.round((float)this.calcification / 100000.0f * 100.0f * 100.0f) / 100.0f;
    }

    public boolean isCalcified() {
        return this.calcification >= 100000;
    }

    public String getOutputFluidName() {
        return this.outputFluid.getName();
    }

    private static enum outputType {
        NONE(""),
        WATER("ic2.SteamGenerator.output.water"),
        DISTILLEDWATER("ic2.SteamGenerator.output.destiwater"),
        STEAM("ic2.SteamGenerator.output.steam"),
        SUPERHEATEDSTEAM("ic2.SteamGenerator.output.hotsteam");

        private final String name;

        private outputType(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }
    }
}

