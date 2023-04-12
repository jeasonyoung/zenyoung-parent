package top.zenyoung.netty.codec;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import javax.annotation.Nonnull;
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
        preInit();
        initCodec(decoder = getDecoder(), encoder = getEncoder());
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

    /**
     * 初始化前置调用
     */
    protected void preInit() {

    }

    /**
     * 初始化编解码器处理
     *
     * @param decoder 解码器
     * @param encoder 编码器
     */
    protected void initCodec(@Nonnull final R decoder, @Nonnull final T encoder) {

    }
}
