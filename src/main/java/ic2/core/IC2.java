/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.level.Level
 *  org.apache.logging.log4j.LogManager
 */
package ic2.core;

import ic2.core.IC2Achievements;
import ic2.core.audio.AudioManager;
import ic2.core.network.NetworkManager;
import ic2.core.proxy.EnvProxy;
import ic2.core.proxy.SideProxy;
import ic2.core.sound.SoundManager;
import ic2.core.util.Keyboard;
import ic2.core.util.Log;
import ic2.core.util.PriorityExecutor;
import ic2.core.ref.ItemName;
import ic2.core.util.SideGateway;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;

public class IC2 {
    public static final String VERSION = "2.9.162+ex119";
    public static final String MODID = "ic2";
    public static final String RESOURCE_DOMAIN = "ic2";
    public static final String ICON_STACK_NAME = "ic2:tab_icon";
    public static final EnvProxy envProxy;
    public static final SideProxy sideProxy;
    public static final Log log;
    public static final SideGateway<NetworkManager> network;
    public static final Keyboard keyboard;
    public static final AudioManager audioManager;
    public static final SoundManager soundManager;
    public static IC2Achievements achievements;
    public static final CreativeModeTab tabIC2;
    /** ex112 兼容字段：平台抽象（客户端=PlatformClient，服务端=Platform） */
    public static final Platform platform = net.neoforged.fml.loading.FMLEnvironment.dist.isClient()
        ? new PlatformClient() : new Platform();
    public static final int setBlockNotify = 1;
    public static final int setBlockUpdate = 2;
    public static final int setBlockNoUpdateFromClient = 4;
    public static final PriorityExecutor threadPool;
    public static final RandomSource random;
    public static boolean initialized;
    public static boolean suddenlyHoes;
    public static boolean seasonal;

    public static int getSeaLevel(Level level) {
        return level.getSeaLevel();
    }

    public static int getWorldMaxHeight(Level level) {
        return level.getHeight();
    }

    public static int getWorldMinHeight(Level level) {
        return level.getMinBuildHeight();
    }

    public static ResourceLocation getIdentifier(String string) {
        return ResourceLocation.fromNamespaceAndPath("ic2", string);
    }

    public static net.minecraft.core.RegistryAccess getRegistryAccess() {
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        try {
            Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
            Object mc = mcClass.getMethod("getInstance").invoke(null);
            Object level = mcClass.getField("level").get(mc);
            if (level != null) {
                return (net.minecraft.core.RegistryAccess)level.getClass().getMethod("registryAccess").invoke(level);
            }
        }
        catch (Exception exception) {
            // 客户端 level 尚未就绪，返回 null
        }
        return null;
    }

    private static EnvProxy createEnvProxy() {
        String string;
        try {
            Class.forName("net.fabricmc.api.ModInitializer");
            string = "ic2.fabric.EnvProxyFabric";
        }
        catch (ClassNotFoundException classNotFoundException) {
            try {
                Class.forName("net.neoforged.neoforge.common.NeoForge");
                string = "ic2.forge.EnvProxyForge";
            }
            catch (ClassNotFoundException classNotFoundException2) {
                throw new RuntimeException("unknown environment");
            }
        }
        try {
            return (EnvProxy)Class.forName(string).getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    private static SideProxy createSideProxy() {
        String string = envProxy.isClientEnv() ? "ic2.core.proxy.SideProxyClient" : "ic2.core.proxy.SideProxyServer";
        try {
            return (SideProxy)Class.forName(string).getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException(reflectiveOperationException);
        }
    }

    static {
        try {
            new BlockPos(1, 2, 3).offset(2, 3, 4);
        }
        catch (Throwable throwable) {
            throw new Error("IC2 is incompatible with this environment, use the normal IC2 version, not the dev one.", throwable);
        }
        envProxy = IC2.createEnvProxy();
        sideProxy = IC2.createSideProxy();
        log = new Log(LogManager.getLogger((String)"ic2"));
        network = new SideGateway("ic2.core.network.NetworkManager", "ic2.core.network.NetworkManagerClient");
        keyboard = sideProxy.getKeyboard();
        audioManager = sideProxy.getAudioManager();
        soundManager = sideProxy.getSoundManager();
        // 第三十九轮修复（用户实测：电炉接 MFSU 过压仍然崩服）：
        // 1.12.2 在 `IC2.init()` 里执行 `achievements = new IC2Achievements();`（ic2_src_112/ic2/core/IC2.java:311），
        // 迁移版的 static 块初始化了 envProxy/sideProxy/log/network/keyboard/audioManager/soundManager/tabIC2/
        // threadPool/random，**唯独漏了 achievements** —— 于是 `IC2.achievements` 恒为 null，
        // 全部 11 处 `IC2.achievements.issueAchievement(...)` 调用点一执行就 NPE
        // （crash-2026-10-06_19.42.12-server.txt:7 即 `explodeTile` 的 explodeMachine 调用）。
        achievements = new IC2Achievements();
        tabIC2 = envProxy.createItemGroup(IC2.getIdentifier("general"),
            () -> ic2.core.ref.Ic2Items.COPPER_CABLE == null
                ? net.minecraft.world.item.ItemStack.EMPTY
                : new net.minecraft.world.item.ItemStack(ic2.core.ref.Ic2Items.COPPER_CABLE));
        threadPool = new PriorityExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 2));
        random = RandomSource.create();
        initialized = false;
        suddenlyHoes = false;
        seasonal = false;
    }
}

