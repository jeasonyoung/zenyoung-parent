package top.zenyoung.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * 消息解码器基类(byte=>message)
 *
 * @author young
 */
public abstract class BaseByteToMessageDecoder<T extends Message> extends ByteToMessageDecoder {
    @Override
    protected final void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        final T data = decode(ctx, in);
        if (Objects.nonNull(data)) {
            out.add(data);
        }
    }

    protected abstract T decode(@Nonnull final ChannelHandlerContext ctx, @Nonnull final ByteBuf in);
}
