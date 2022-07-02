package top.zenyoung.netty.codec;

import javax.annotation.Nonnull;

/**
 * 消息编解码器接口
 *
 * @author young
 */
public interface MessageCodec<T extends MessageEncoder<? extends Message>, R extends MessageDecoder<? extends Message>> {

    /**
     * 获取编码器
     *
     * @return 编码器
     */
    @Nonnull
    T getEncoder();

    /**
     * 获取解码器
     *
     * @return 解码器
     */
    @Nonnull
    R getDecoder();
}
