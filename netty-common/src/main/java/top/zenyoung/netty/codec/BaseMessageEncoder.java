package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Optional;

/**
 * 消息编码接口
 *
 * @author young
 */
@ChannelHandler.Sharable
public abstract class BaseMessageEncoder<T extends Message> extends ChannelOutboundHandlerAdapter implements MessageEncoder<T> {

    @Override
    @SuppressWarnings({"unchecked"})
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof Message) {
            final T cast = (T) msg;
            Optional.of(cast)
                    .map(in -> encode(ctx.alloc(), in))
                    .ifPresent(out -> ctx.write(out, promise));
        } else {
            ctx.write(msg, promise);
        }
    }
}
