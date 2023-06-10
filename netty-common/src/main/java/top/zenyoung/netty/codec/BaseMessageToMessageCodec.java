package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * 消息编解码器基类(message<=>message)
 *
 * @author young
 */
public abstract class BaseMessageToMessageCodec<R, T extends Message> extends MessageToMessageCodec<R, T> {

    @Override
    protected final void encode(final ChannelHandlerContext ctx, final T msg, final List<Object> out) {
        final R data = encode(ctx, msg);
        if (Objects.nonNull(data)) {
            out.add(data);
        }
    }

    protected abstract R encode(@Nonnull final ChannelHandlerContext ctx, @Nonnull final T msg);

    @Override
    protected final void decode(final ChannelHandlerContext ctx, final R msg, final List<Object> out) {
        final T data = decode(ctx, msg);
        if (Objects.nonNull(data)) {
            out.add(data);
        }
    }

    protected abstract T decode(@Nonnull final ChannelHandlerContext ctx, @Nonnull final R msg);
}
