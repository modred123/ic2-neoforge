package ic2.core;

/**
 * 客户端 Platform 实现（ex112 兼容层）。
 * 客户端渲染注册由 1.19.2 的 SideProxyClient（ic2.core.proxy）负责，
 * 本类只保留 platform 语义（isRendering/isSimulating 等）。
 */
public class PlatformClient
extends Platform {
    @Override
    public boolean isRendering() {
        // 第四十五轮修复（P0）：原实现恒返回 true，而 `Platform.isSimulating()` = `!isRendering()` ——
        // 于是**单人游戏的集成服务端线程**也被当成客户端，所有 `IC2.platform.isSimulating()` 分支
        // 都走错一侧。实测症状：电表「重置」在服务端被当成客户端 → 再次发送容器事件 →
        // 与 `ContainerMeter.onContainerEvent` 互相触发形成死循环（同一毫秒刷满日志，
        // latest.log 涨到 1.17 GB），`resultCount` 永远清不掉（用户反馈"重置没反应"）。
        // 1.12.2 的权威语义是 `FMLCommonHandler.getEffectiveSide().isClient()` —— 按**当前线程**判断端；
        // 1.21 的对等物就是"当前线程是否为主客户端线程"。
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        return minecraft != null && minecraft.isSameThread();
    }

    public net.minecraft.client.Minecraft getMinecraftInstance() {
        return net.minecraft.client.Minecraft.getInstance();
    }
}
