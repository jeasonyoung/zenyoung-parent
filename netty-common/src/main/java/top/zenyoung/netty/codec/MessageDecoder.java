package top.zenyoung.netty.codec;

import javax.annotation.Nonnull;

/**
 * 消息解码
 *
 * @author young
 */
public interface MessageDecoder<T extends Message> {
    /**
     * 消息解码
     *
     * @param in 消息缓存
     * @return 解码后的对象
     */
    T decoder(@Nonnull final Object in);
}
