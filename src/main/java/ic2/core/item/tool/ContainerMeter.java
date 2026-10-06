/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.network.ClientModifiable;
import ic2.core.ContainerFullInv;
import ic2.core.IC2;
import ic2.core.item.tool.HandHeldMeter;
import ic2.core.util.LogCategory;
import net.minecraft.world.entity.player.Player;

public class ContainerMeter
extends ContainerFullInv<HandHeldMeter> {
    private IEnergyTile uut;
    private double resultAvg;
    private double resultMin;
    private double resultMax;
    private int resultCount = 0;
    @ClientModifiable
    private Mode mode = Mode.EnergyIn;

    public ContainerMeter(int n, net.minecraft.world.entity.player.Inventory inventory, HandHeldMeter meter) {
        // 第三十三轮修复：原先错用 DYNAMIC_ITEM（其界面工厂要求 DynamicContainer）→ 右键电表客户端强转失败闪退。
        super(ic2.core.ref.Ic2ScreenHandlers.METER, n, inventory, meter, 218);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.uut == null) {
            return;
        }
        NodeStats stats = EnergyNet.instance.getNodeStats(this.uut);
        if (stats == null) {
            ((HandHeldMeter)this.base).closeGUI();
            return;
        }
        double result = 0.0;
        switch (this.mode) {
            case EnergyIn: {
                result = stats.getEnergyIn();
                break;
            }
            case EnergyOut: {
                result = stats.getEnergyOut();
                break;
            }
            case EnergyGain: {
                result = stats.getEnergyIn() - stats.getEnergyOut();
                break;
            }
            case Voltage: {
                result = stats.getVoltage();
            }
        }
        if (this.resultCount == 0) {
            this.resultMin = this.resultMax = result;
            this.resultAvg = this.resultMax;
        } else {
            if (result < this.resultMin) {
                this.resultMin = result;
            }
            if (result > this.resultMax) {
                this.resultMax = result;
            }
            this.resultAvg = (this.resultAvg * (double)this.resultCount + result) / (double)(this.resultCount + 1);
        }
        // 第四十三轮诊断：电压模式读数与代码语义不符（用户报告显示 2.048 kV，而 `NodeStats.voltage`
        // 由 `getTierFromPower(...)` 产出、语义上是 0-5 的等级）—— 打前三条原始读数即可定论。
        if (this.resultCount == 0) {
            IC2.log.info(LogCategory.General, "[MeterDiag] mode=%s result=%.6f avg=%.6f min=%.6f max=%.6f", this.mode, result, this.resultAvg, this.resultMin, this.resultMax);
        }
        ++this.resultCount;
        IC2.network.get(true).sendContainerFields(this, "resultAvg", "resultMin", "resultMax", "resultCount");
    }

    public double getResultAvg() {
        return this.resultAvg;
    }

    public double getResultMin() {
        return this.resultMin;
    }

    public double getResultMax() {
        return this.resultMax;
    }

    public int getResultCount() {
        return this.resultCount;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        IC2.network.get(false).sendContainerField(this, "mode");
        this.reset();
    }

    public void reset() {
        if (IC2.platform.isSimulating()) {
            this.resultCount = 0;
        } else {
            IC2.network.get(false).sendContainerEvent(this, "reset");
        }
    }

    public void setUut(IEnergyTile uut) {
        assert (this.uut == null);
        this.uut = uut;
    }

    @Override
    public void onContainerEvent(String event) {
        super.onContainerEvent(event);
        // 第四十五轮修复：服务端语义直接清零，不再回调 `reset()` ——
        // 否则一旦端判断出错（见 `PlatformClient.isRendering` 的修复）就会无限互发事件。
        if ("reset".equals(event)) {
            this.resultCount = 0;
        }
    }

    public static enum Mode {
        EnergyIn,
        EnergyOut,
        EnergyGain,
        Voltage;

    }
}

