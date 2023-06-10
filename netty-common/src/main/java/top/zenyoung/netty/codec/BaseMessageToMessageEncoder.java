package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * 消息编码器基类(message=>message)
 *
 * @author young
 */
public abstract class BaseMessageToMessageEncoder<T extends Message, R> extends MessageToMessageEncoder<T> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final T msg, final List<Object> out) {
        final R data = encode(ctx, msg);
        if (Objects.nonNull(data)) {
            out.add(data);
        }
    }

    protected abstract R encode(@Nonnull final ChannelHandlerContext ctx, @Nonnull final T msg);
}
