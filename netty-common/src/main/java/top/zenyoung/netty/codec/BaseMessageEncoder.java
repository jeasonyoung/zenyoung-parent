package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;
import java.util.Optional;

/**
 * 消息编码接口
 *
 * @author young
 */
@ChannelHandler.Sharable
public abstract class BaseMessageEncoder<T extends Message> extends MessageToMessageEncoder<T> implements MessageEncoder<T> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final T msg, final List<Object> out) {
        Optional.ofNullable(encode(ctx.alloc(), msg))
                .ifPresent(out::add);
    }
}
