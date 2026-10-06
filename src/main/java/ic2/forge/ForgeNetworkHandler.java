package ic2.forge;

import ic2.core.IC2;
import ic2.core.network.Ic2Payload;
import ic2.core.network.Ic2ServerPayload;
import ic2.core.util.LogCategory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ForgeNetworkHandler {
    /** 幂等保护：同时用两条路径注册（modEventBus.addListener 与 @SubscribeEvent），
     *  谁先被 NeoForge 回调谁生效，第二条静默忽略，避免重复注册 payload 导致 NeoForge 报错。
     *  这样无论哪条路径可靠，网络都能注册上；而探针会记录实际生效的路径。 */
    private static final java.util.concurrent.atomic.AtomicBoolean REGISTERED = new java.util.concurrent.atomic.AtomicBoolean(false);

    ForgeNetworkHandler() {
    }

    /**
     * 标记本次调用来自哪条注册路径，仅用于诊断。
     * @param source "addListener" 或 "subscribeEvent"
     */
    public static void register(RegisterPayloadHandlersEvent event, String source) {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        register(event);
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        try {
            PayloadRegistrar registrar = event.registrar("ic2");

            registrar.playToClient(Ic2Payload.TYPE, Ic2Payload.STREAM_CODEC, (payload, context) -> {
                context.enqueueWork(() -> {
                    IC2.network.get(false).onPacket(io.netty.buffer.Unpooled.wrappedBuffer(payload.data()), IC2.sideProxy.getPlayerInstance());
                });
            });

            registrar.playToServer(Ic2ServerPayload.TYPE, Ic2ServerPayload.STREAM_CODEC, (payload, context) -> {
                context.enqueueWork(() -> {
                    IC2.network.get(true).onPacket(io.netty.buffer.Unpooled.wrappedBuffer(payload.data()), context.player());
                });
            });
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-diag")
                    .error("payload registration failed", t);
            throw t;
        }
    }
}
