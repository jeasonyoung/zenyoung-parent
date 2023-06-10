package top.zenyoung.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * 消息编解码器基类(byte<=>message)
 *
 * @param <T> 消息类型
 * @author young
 */
public abstract class BaseByteToMessageCodec<T extends Message> extends ByteToMessageCodec<T> {

    @Override
    protected final void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        final T data = decode(ctx, in);
        if (Objects.nonNull(data)) {
            out.add(data);
        }
    }

    protected abstract T decode(@Nonnull final ChannelHandlerContext ctx, @Nonnull final ByteBuf in);
}
