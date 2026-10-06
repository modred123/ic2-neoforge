/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.LightLayer
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.block.generator.tileentity;

import ic2.core.IC2;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.BiomeUtil;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySolarGenerator
extends TileEntityBaseGenerator {
    @GuiSynced
    public float skyLight;
    private int ticker = IC2.random.nextInt(128);
    private static final int tickRate = 128;
    private static final double energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/solar");
    /**
     * 诊断用（2026-09-01 改进）：原设计只在首次打一条日志，无法区分「TE 根本没 tick」与
     * 「tick 正常但探针只打一次」。现改为前 5 次全打，之后每 32 次打一次，
     * 这样既能看到首次值，也能确认 128 tick 周期的刷新是否真的在跑。
     */

    public TileEntitySolarGenerator(BlockPos blockPos, BlockState blockState) {
        super(Ic2BlockEntities.SOLAR_GENERATOR, blockPos, blockState, 1.0, 1, 2);
    }

    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateSunVisibility();
    }

    @Override
    public boolean gainEnergy() {
        if (++this.ticker % 128 == 0) {
            this.updateSunVisibility();
        }
        if (this.skyLight > 0.0f) {
            this.energy.addEnergy(energyMultiplier * (double)this.skyLight);
            return true;
        }
        return false;
    }

    @Override
    public boolean gainFuel() {
        return false;
    }

    public void updateSunVisibility() {
        this.skyLight = TileEntitySolarGenerator.getSkyLight(this.getLevel(), this.worldPosition.above());
    }

    public static float getSkyLight(Level level, BlockPos blockPos) {
        if (!level.dimensionType().hasSkyLight()) {
            return 0.0f;
        }
        float f = Util.limit((float)Math.cos(level.getSunAngle(1.0f)) * 2.0f + 0.2f, 0.0f, 1.0f);
        if (!IC2.envProxy.biomeHasType(BiomeUtil.getBiome((LevelReader)level, blockPos), EnvProxy.BiomeType.SANDY)) {
            f *= 1.0f - level.getRainLevel(1.0f) * 5.0f / 16.0f;
            f *= 1.0f - level.getThunderLevel(1.0f) * 5.0f / 16.0f;
            f = Util.limit(f, 0.0f, 1.0f);
        }
        return (float)level.getBrightness(LightLayer.SKY, blockPos) / 15.0f * f;
    }

    @Override
    public boolean needsFuel() {
        return false;
    }

    @Override
    public boolean getGuiState(String string) {
        if ("sunlight".equals(string)) {
            return this.skyLight > 0.0f;
        }
        return super.getGuiState(string);
    }

    @Override
    protected boolean delayActiveUpdate() {
        return true;
    }
}

