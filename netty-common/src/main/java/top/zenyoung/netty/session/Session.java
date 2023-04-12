package top.zenyoung.netty.session;

import io.netty.channel.ChannelFutureListener;
import top.zenyoung.netty.codec.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Socket通信会话
 *
 * @author young
 */
public interface Session extends Serializable {
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
     * @param data     消息数据
     * @param listener 发送结果监听
     */
    void send(@Nonnull final Message data, @Nullable final ChannelFutureListener listener);

    /**
     * 发送消息
     *
     * @param data 消息数据
     */
    default void send(@Nonnull final Message data) {
        this.send(data, null);
    }

    /**
     * 关闭会话
     */
    void close();
}
