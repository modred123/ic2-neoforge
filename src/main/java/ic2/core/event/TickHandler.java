/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.Level
 */
package ic2.core.event;

import ic2.core.IC2;
import ic2.core.event.IWorldTickCallback;
import ic2.core.event.WorldData;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.level.Level;

public class TickHandler {
    private static final boolean debugupdate = System.getProperty("ic2.debugupdate") != null;
    private static final Map<IWorldTickCallback, Throwable> debugTraces = debugupdate ? new WeakHashMap() : null;
    private static Throwable lastDebugTrace;
    // 1.21 迁移诊断：一次性日志，确认游戏总线 LevelTickEvent → TickHandler 链已接通
    // （若进入世界后 debug.log 出现本行，说明事件注册修复生效、EnergyNet 开始 tick）。
    private static volatile boolean loggedWorldTickOnce = false;
    private static volatile boolean loggedClientTickOnce = false;

    public static void onWorldTickStart(Level level) {
        if (level.isClientSide) {
            return;
        }
        if (!loggedWorldTickOnce) {
            loggedWorldTickOnce = true;
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag").info(
                "TickHandler.onWorldTickStart FIRST call, level={} — game bus tick chain ACTIVE",
                level.dimension().location());
        }
        WorldData worldData = WorldData.get(level, false);
        if (worldData == null) {
            return;
        }
        level.getProfiler().push("updates");
        TickHandler.processUpdates(level, worldData);
        level.getProfiler().popPush("Wind");
        worldData.windSim.updateWind();
        level.getProfiler().popPush("energynet");
        worldData.energyNet.onTickStart();
        level.getProfiler().pop();
    }

    public static void onWorldTickEnd(Level level) {
        if (level.isClientSide) {
            return;
        }
        WorldData worldData = WorldData.get(level, false);
        if (worldData == null) {
            return;
        }
        level.getProfiler().push("energynet");
        worldData.energyNet.onTickEnd();
        level.getProfiler().popPush("Networking");
        IC2.network.get(true).onTickEnd(worldData);
        level.getProfiler().pop();
    }

    public static void onServerTick() {
    }

    public static void onClientTick() {
        IC2.keyboard.sendKeyUpdate();
        IC2.audioManager.onTick();
        IC2.soundManager.tick();
        Level level = IC2.sideProxy.getPlayerWorld();
        if (level != null) {
            TickHandler.processUpdates(level, WorldData.get(level));
        }
    }

    public static void requestSingleWorldTick(Level level, IWorldTickCallback iWorldTickCallback) {
        WorldData.get((Level)level).singleUpdates.add(iWorldTickCallback);
        if (debugupdate) {
            debugTraces.put(iWorldTickCallback, new Throwable());
        }
    }

    public static void requestContinuousWorldTick(Level level, IWorldTickCallback iWorldTickCallback) {
        WorldData worldData = WorldData.get(level);
        if (!worldData.continuousUpdatesInUse) {
            worldData.continuousUpdates.add(iWorldTickCallback);
        } else {
            worldData.continuousUpdatesToRemove.remove(iWorldTickCallback);
            worldData.continuousUpdatesToAdd.add(iWorldTickCallback);
        }
        if (debugupdate) {
            debugTraces.put(iWorldTickCallback, new Throwable());
        }
    }

    public static void removeContinuousWorldTick(Level level, IWorldTickCallback iWorldTickCallback) {
        WorldData worldData = WorldData.get(level);
        if (!worldData.continuousUpdatesInUse) {
            worldData.continuousUpdates.remove(iWorldTickCallback);
        } else {
            worldData.continuousUpdatesToAdd.remove(iWorldTickCallback);
            worldData.continuousUpdatesToRemove.add(iWorldTickCallback);
        }
    }

    public static Throwable getLastDebugTrace() {
        return lastDebugTrace;
    }

    private static void processUpdates(Level level, WorldData worldData) {
        IWorldTickCallback iWorldTickCallback;
        level.getProfiler().push("single-update");
        while ((iWorldTickCallback = worldData.singleUpdates.poll()) != null) {
            if (debugupdate) {
                lastDebugTrace = debugTraces.remove(iWorldTickCallback);
            }
            iWorldTickCallback.onTick(level);
        }
        level.getProfiler().popPush("cont-update");
        worldData.continuousUpdatesInUse = true;
        for (IWorldTickCallback iWorldTickCallback2 : worldData.continuousUpdates) {
            if (debugupdate) {
                lastDebugTrace = debugTraces.remove(iWorldTickCallback2);
            }
            iWorldTickCallback2.onTick(level);
        }
        worldData.continuousUpdatesInUse = false;
        if (debugupdate) {
            lastDebugTrace = null;
        }
        worldData.continuousUpdates.addAll(worldData.continuousUpdatesToAdd);
        worldData.continuousUpdatesToAdd.clear();
        worldData.continuousUpdates.removeAll(worldData.continuousUpdatesToRemove);
        worldData.continuousUpdatesToRemove.clear();
        level.getProfiler().pop();
    }
}

