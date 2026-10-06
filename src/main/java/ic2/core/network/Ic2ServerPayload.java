package ic2.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * IC2 C2S payload 载体。
 * <p>
 * 1.21.1 修复（第二十二轮）：与 {@link Ic2Payload} 同因——原 codec 用
 * {@code writeBytes(ByteBuf)} 编码，会消耗源缓冲区 readerIndex；NeoForge 的
 * {@code GenericPacketSplitter} 会先测量编码一次、{@code PacketEncoder} 再编码一次，
 * 第二次即写出 0 字节。改用 {@code byte[]} 承载，编码无副作用。
 * <p>
 * 1.21.1 修复（第二十三轮）：与 {@link Ic2Payload} 对称——解码器必须**消费**掉 buffer 中
 * 全部剩余字节（原版 {@code PacketDecoder} 解包后会校验 {@code readableBytes() == 0}，
 * 否则抛 "found N bytes extra whilst reading packet" 并断连）。
 * 故用 {@code readBytes(byte[])} 取代只读不跳的 {@code ByteBufUtil.getBytes(ByteBuf)}。
 */
public record Ic2ServerPayload(byte[] data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<Ic2ServerPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("ic2", "m_server"));

    public static final StreamCodec<ByteBuf, Ic2ServerPayload> STREAM_CODEC = StreamCodec.of(
        (byteBuf, ic2ServerPayload) -> byteBuf.writeBytes(ic2ServerPayload.data),
        byteBuf -> {
            byte[] data = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(data);
            return new Ic2ServerPayload(data);
        }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
