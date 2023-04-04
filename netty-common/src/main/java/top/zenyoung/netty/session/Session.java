package top.zenyoung.netty.session;

import io.netty.channel.ChannelFutureListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.Serializable;

/**
 * Socket通信会话
 *
 * @author young
 */
public interface Session extends Serializable, Closeable {
    /**
     * 获取客户端设备ID。
     *
     * @return 客户端设备ID。
     */
    String getDeviceId();

    /**
     * 获取客户端IP地址。
     *
     * @return 客户端IP地址。
     */
    String getClientIp();

    /**
     * 获取会话状态
     *
     * @return 会话状态
     */
    boolean getStatus();

    /**
     * 读取通道数据
     */
    void readChannelData();

    /**
     * 发送消息
     *
     * @param content  消息内容。
     * @param listener 发送结果监听。
     * @param <T>      消息数据类型
     */
    <T> void send(@Nonnull final T content, @Nullable final ChannelFutureListener listener);

    /**
     * 发送消息
     *
     * @param content 消息内容
     * @param <T>     消息数据类型
     */
    default <T> void send(@Nonnull final T content) {
        this.send(content, null);
    }
}
