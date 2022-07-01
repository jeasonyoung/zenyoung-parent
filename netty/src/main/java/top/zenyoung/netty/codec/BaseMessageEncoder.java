package top.zenyoung.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;
import java.util.Objects;

/**
 * 消息编码接口
 *
 * @author young
 */
@ChannelHandler.Sharable
public abstract class BaseMessageEncoder<T extends Message> extends MessageToMessageEncoder<T> implements MessageEncoder<T> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final T msg, final List<Object> out) {
        final ByteBuf buf = encode(ctx.alloc(), msg);
        if (Objects.nonNull(buf)) {
            out.add(buf);
        }
    }
}
