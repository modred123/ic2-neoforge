package ic2.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * IC2 S2C payload 载体。
 * <p>
 * 1.21.1 修复（第二十二轮）：历史上 data 字段是 {@code ByteBuf}，codec 编码器写成
 * {@code byteBuf.writeBytes(payload.data)}。{@code writeBytes(ByteBuf)} 会推进源缓冲区的
 * readerIndex——也就是编码本身带副作用。
 * <p>
 * 而 NeoForge 的 {@code GenericPacketSplitter}（出站包大小检查器）会对**每一个**出站包先
 * 调用一次 {@code protocolInfo.codec().encode(tmpBuf, packet)} 来测量大小，随后真正的
 * {@code PacketEncoder} 再编码一次。于是第一次编码把源缓冲区读空，第二次编码写出 0 字节，
 * 对端解码得到空 payload（实测日志 {@code [NET-s2c] handler INVOKED, bytes=0}）。
 * <p>
 * 改为持有的 {@code byte[]}：编码走 {@code writeBytes(byte[])}，无任何副作用，可重复编码。
 * <p>
 * 1.21.1 修复（第二十三轮）：**解码器必须把 buffer 读完**。原 decoder 用
 * {@code ByteBufUtil.getBytes(byteBuf)} 取数据，但该方法等价于
 * {@code getBytes(buf, buf.readerIndex(), buf.readableBytes())}——纯读取，**不推进 readerIndex**。
 * 而原版 {@code PacketDecoder.decode()} 在解包后有强制校验：
 * <pre>
 * if (p_130536_.readableBytes() &gt; 0) {
 *     throw new IOException("Packet ... was larger than I expected, found "
 *         + p_130536_.readableBytes() + " bytes extra whilst reading packet ...");
 * }
 * </pre>
 * 于是解码后剩余字节数 = payload 长度，直接抛异常断连（实测报错 "found 52 bytes extra"）。
 * <p>
 * 原版自身的兜底实现 {@code DiscardedPayload} 就是示范：解码分支里 {@code buf.skipBytes(i)}
 * 把剩余字节全部消费。故此处改用 {@code byteBuf.readBytes(byte[])}——既取出数据，又推进 readerIndex。
 */
public record Ic2Payload(byte[] data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<Ic2Payload> TYPE = new CustomPacketPayload.Type<>(NetworkManager.channelId);

    public static final StreamCodec<ByteBuf, Ic2Payload> STREAM_CODEC = StreamCodec.of(
        (byteBuf, ic2Payload) -> byteBuf.writeBytes(ic2Payload.data),
        byteBuf -> {
            byte[] data = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(data);
            return new Ic2Payload(data);
        }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
