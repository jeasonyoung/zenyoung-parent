package top.zenyoung.netty.codec;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.Objects;

/**
 * 消息编解码器抽象基类
 *
 * @author young
 */
public abstract class BaseMessageCodec<T extends BaseMessageEncoder<? extends Message>, R extends BaseMessageDecoder<? extends Message>>
        extends ChannelDuplexHandler implements MessageCodec<T, R> {
    private final T encoder;
    private final R decoder;

    /**
     * 构造函数
     */
    public BaseMessageCodec() {
        this.encoder = getEncoder();
        this.decoder = getDecoder();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (Objects.nonNull(decoder)) {
            decoder.channelRead(ctx, msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if (Objects.nonNull(encoder)) {
            encoder.write(ctx, msg, promise);
        } else {
            super.write(ctx, msg, promise);
        }
    }
}
