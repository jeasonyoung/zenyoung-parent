package top.zenyoung.netty.client.handler;

import io.netty.channel.Channel;

import javax.annotation.Nonnull;

/**
 * 客户端连接成功处理器
 *
 * @author young
 */
@FunctionalInterface
public interface ConnectedHandler {

    /**
     * 连接成功处理
     *
     * @param channel 连接通道
     */
    void handler(@Nonnull final Channel channel);
}
