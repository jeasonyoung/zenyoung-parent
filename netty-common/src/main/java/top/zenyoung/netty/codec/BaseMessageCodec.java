package top.zenyoung.netty.codec;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * 消息编解码器抽象基类
 *
 * @author young
 */
public abstract class BaseMessageCodec<T extends BaseMessageEncoder<? extends Message>, R extends BaseMessageDecoder<? extends Message>>
        extends ChannelDuplexHandler implements MessageCodec<T, R> {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final R decoder = getDecoder();
        decoder.channelRead(ctx, msg);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        final T encoder = getEncoder();
        encoder.write(ctx, msg, promise);
    }
}
