package top.zenyoung.netty.codec;

import io.netty.channel.CombinedChannelDuplexHandler;

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
        this.init(getDecoder(), getEncoder());
    }
}
