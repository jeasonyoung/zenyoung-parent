package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * 消息解码器基类(message<=message)
 *
 * @param <T> 上级消息
 * @param <R> 二次解码后消息
 * @author young
 */
public abstract class BaseMessageToMessageDecoder<T, R extends Message> extends MessageToMessageDecoder<T> {

    @Override
    protected final void decode(final ChannelHandlerContext ctx, final T msg, final List<Object> out) {
        final R data = decode(ctx, msg);
        if (Objects.nonNull(data)) {
            out.add(data);
        }
    }

    protected abstract R decode(@Nonnull final ChannelHandlerContext ctx, @Nonnull final T msg);
}
