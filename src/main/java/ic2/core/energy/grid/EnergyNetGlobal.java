/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core.energy.grid;

import ic2.api.energy.IEnergyNet;
import ic2.api.energy.IEnergyNetEventReceiver;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.core.IC2;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.core.energy.grid.IEnergyCalculator;
import ic2.core.energy.leg.EnergyCalculatorLeg;
import ic2.core.event.WorldData;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnergyNetGlobal
implements IEnergyNet {
    private static final List<IEnergyNetEventReceiver> eventReceivers = new CopyOnWriteArrayList<IEnergyNetEventReceiver>();
    private static IEnergyCalculator calculator;
    /** 诊断用（2026-09-23）：addTile 只在首次调用时打一条日志，避免刷屏。 */
    private static final java.util.concurrent.atomic.AtomicBoolean diagAddTileLogged = new java.util.concurrent.atomic.AtomicBoolean(false);

    public static IEnergyNet create() {
        // 1.21 迁移修复（第十八轮）：恢复原版语义——create() 恒返 EnergyNetGlobal（ic2_src 实证）。
        // 第十四轮的 stub 分支是错误修复：create() 在 mod 加载期（RegisterEvent/onInitEarly）调用，
        // 此时 integrated server 必然尚未创建 → 单机恒命中 isClientEnv 分支 → EnergyNet.instance
        // 是 no-op stub → 电缆/机器的 addTile 全被吞 → 能量网永远为空（电线不通问题的源头）。
        // 客户端安全性由调用点守卫保证（Energy.onLoaded 的 !isClientSide、cable addToEnet/
        // removeFromEnet 的 isClientSide 早退、各 TE onLoaded/onUnloaded 的 !isClientSide，原版同款）。
        // 注意：恢复真实能量网前已全量扫描并修复 CFR 自递归雷（Tile.getAmount、Change.getAmount），
        // 否则 GridUpdater 一跑就 StackOverflowError（22:24 崩溃教训）。
        calculator = new EnergyCalculatorLeg();
        return new EnergyNetGlobal();
    }

    private EnergyNetGlobal() {
    }

    @Override
    public IEnergyTile getTile(Level level, BlockPos blockPos) {
        if (level == null) {
            throw new NullPointerException("null world");
        }
        if (blockPos == null) {
            throw new NullPointerException("null pos");
        }
        return EnergyNetGlobal.getLocal(level).getIoTile(blockPos);
    }

    @Override
    public IEnergyTile getSubTile(Level level, BlockPos blockPos) {
        if (level == null) {
            throw new NullPointerException("null world");
        }
        if (blockPos == null) {
            throw new NullPointerException("null pos");
        }
        return EnergyNetGlobal.getLocal(level).getSubTile(blockPos);
    }

    @Override
    public <T extends BlockEntity> void addBlockEntityTile(T t) {
        if (t == null) {
            throw new NullPointerException("null tile");
        }
        EnergyNetGlobal.addTile((IEnergyTile)t, t.getLevel(), t.getBlockPos());
    }

    @Override
    public <T extends ILocatable & IEnergyTile> void addLocatableTile(T t) {
        if (t == null) {
            throw new NullPointerException("null tile");
        }
        EnergyNetGlobal.addTile(t, t.getWorldObj(), t.getPosition());
    }

    private static void addTile(IEnergyTile iEnergyTile, Level level, BlockPos blockPos) {
        // 诊断（2026-09-23，一次性）：确认能量方块/电缆是否真的入网，以及 EnergyNet.instance 的实际实现类。
        // 若实测日志缺此行 → 入网调用链（电缆 addToEnet / Energy 组件 onLoaded）未触发；
        // 若 instance 显示为 stub 类 → create() 分支问题复发。
        if (diagAddTileLogged.compareAndSet(false, true)) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").info(
                "EnergyNet.addTile FIRST call: tile={} pos={} enetImpl={}",
                iEnergyTile.getClass().getSimpleName(), blockPos,
                ic2.api.energy.EnergyNet.instance == null ? "null" : ic2.api.energy.EnergyNet.instance.getClass().getSimpleName());
        }
        if (EnergyNetSettings.logEnetApiAccessTraces) {
            IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API addTile %s.", Util.toString(iEnergyTile, (BlockGetter)level, blockPos));
        } else if (EnergyNetSettings.logEnetApiAccesses) {
            IC2.log.debug(LogCategory.EnergyNet, "API addTile %s.", Util.toString(iEnergyTile, (BlockGetter)level, blockPos));
        }
        EnergyNetGlobal.getLocal(level).addTile(iEnergyTile, blockPos);
    }

    @Override
    public void removeTile(IEnergyTile iEnergyTile) {
        if (iEnergyTile == null) {
            throw new NullPointerException("null tile");
        }
        Level level = this.getWorld(iEnergyTile);
        BlockPos blockPos = this.getPos(iEnergyTile);
        if (EnergyNetSettings.logEnetApiAccessTraces) {
            IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API removeTile %s.", Util.toString(iEnergyTile, (BlockGetter)level, blockPos));
        } else if (EnergyNetSettings.logEnetApiAccesses) {
            IC2.log.debug(LogCategory.EnergyNet, "API removeTile %s.", Util.toString(iEnergyTile, (BlockGetter)level, blockPos));
        }
        EnergyNetGlobal.getLocal(level).removeTile(iEnergyTile, blockPos);
    }

    @Override
    public Level getWorld(IEnergyTile iEnergyTile) {
        if (iEnergyTile == null) {
            throw new NullPointerException("null tile");
        }
        if (iEnergyTile instanceof ILocatable) {
            return ((ILocatable)((Object)iEnergyTile)).getWorldObj();
        }
        if (iEnergyTile instanceof BlockEntity) {
            return ((BlockEntity)iEnergyTile).getLevel();
        }
        throw new UnsupportedOperationException("unlocatable tile type: " + iEnergyTile.getClass().getName());
    }

    @Override
    public BlockPos getPos(IEnergyTile iEnergyTile) {
        if (iEnergyTile == null) {
            throw new NullPointerException("null tile");
        }
        if (iEnergyTile instanceof ILocatable) {
            return ((ILocatable)((Object)iEnergyTile)).getPosition();
        }
        if (iEnergyTile instanceof BlockEntity) {
            return ((BlockEntity)iEnergyTile).getBlockPos();
        }
        throw new UnsupportedOperationException("unlocatable tile type: " + iEnergyTile.getClass().getName());
    }

    @Override
    public NodeStats getNodeStats(IEnergyTile iEnergyTile) {
        return EnergyNetGlobal.getLocal(this.getWorld(iEnergyTile)).getNodeStats(iEnergyTile);
    }

    @Override
    public int getAdjacentConnections(IEnergyTile iEnergyTile) {
        return EnergyNetGlobal.getLocal(this.getWorld(iEnergyTile)).getAdjacentConnections(iEnergyTile);
    }

    public boolean dumpDebugInfo(Level level, BlockPos blockPos, PrintStream printStream, PrintStream printStream2) {
        return EnergyNetGlobal.getLocal(level).dumpDebugInfo(blockPos, printStream, printStream2);
    }

    @Override
    public double getPowerFromTier(int n) {
        if (n < 14) {
            return 8 << n * 2;
        }
        if (n < 30) {
            return 8.0 * Math.pow(4.0, n);
        }
        return 9.223372036854776E18;
    }

    @Override
    public int getTierFromPower(double d) {
        if (d <= 0.0) {
            return 0;
        }
        return (int)Math.ceil(Math.log(d / 8.0) / Math.log(4.0));
    }

    @Override
    public synchronized void registerEventReceiver(IEnergyNetEventReceiver iEnergyNetEventReceiver) {
        if (eventReceivers.contains(iEnergyNetEventReceiver)) {
            return;
        }
        eventReceivers.add(iEnergyNetEventReceiver);
    }

    @Override
    public synchronized void unregisterEventReceiver(IEnergyNetEventReceiver iEnergyNetEventReceiver) {
        eventReceivers.remove(iEnergyNetEventReceiver);
    }

    static Iterable<IEnergyNetEventReceiver> getEventReceivers() {
        return eventReceivers;
    }

    static IEnergyCalculator getCalculator() {
        return calculator;
    }

    public static EnergyNetLocal getLocal(Level level) {
        if (level.isClientSide) {
            throw new IllegalStateException("not applicable clientside");
        }
        assert (level.getServer().isSameThread());
        return WorldData.get((Level)level).energyNet;
    }
}

