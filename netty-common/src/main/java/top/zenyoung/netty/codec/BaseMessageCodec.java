package top.zenyoung.netty.codec;

import io.netty.channel.CombinedChannelDuplexHandler;

import javax.annotation.Nonnull;

/**
 * 消息编解码器抽象基类
 *
 * @author young
 */
public abstract class BaseMessageCodec<T extends BaseMessageEncoder<? extends Message>, R extends BaseMessageDecoder<? extends Message>>
        extends CombinedChannelDuplexHandler<R, T> implements MessageCodec<T, R> {
    /**
     * 构造函数
     */
    public BaseMessageCodec() {
        super();
        this.initCodec(getDecoder(), getEncoder());
    }

    /**
     * 初始化编解码器
     *
     * @param decoder 解码器
     * @param encoder 编码器
     */
    protected void initCodec(@Nonnull final R decoder, @Nonnull final T encoder) {
        this.init(decoder, encoder);
    }
}
