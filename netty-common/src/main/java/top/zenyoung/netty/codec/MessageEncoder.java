package top.zenyoung.netty.codec;

import io.netty.buffer.ByteBufAllocator;

import javax.annotation.Nonnull;

/**
 * 消息编码器
 *
 * @author young
 */
public interface MessageEncoder<T extends Message> {

    /**
     * 消息编码
     *
     * @param allocator 内存分配器
     * @param message   消息数据
     * @return 编码后的字节
     */
    Object encode(@Nonnull final ByteBufAllocator allocator, @Nonnull final T message);
}
