package top.zenyoung.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * 消息解码器
 *
 * @author young
 */
public abstract class BaseMessageDecoder<T extends Message> extends ChannelInboundHandlerAdapter implements MessageDecoder<T> {

    @Override
    public final void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (Objects.nonNull(ctx) && Objects.nonNull(msg)) {
            decoder(ctx, msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * 解码处理
     *
     * @param ctx 通道上下文
     * @param in  通道数据
     */
    protected void decoder(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Object in) {
        final T out = decoder(in);
        if (Objects.nonNull(out)) {
            ctx.fireChannelRead(out);
        }
    }
}
