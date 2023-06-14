package top.zenyoung.netty.session;

import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeMap;
import top.zenyoung.netty.codec.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Socket通信会话
 *
 * @author young
 */
public interface Session extends AttributeMap, Serializable {
    /**
     * 获取通道ID
     *
     * @return 通道ID
     */
    String getChannelId();

    /**
     * 获取客户端设备ID。
     *
     * @return 客户端设备ID。
     */
    String getDeviceId();

    /**
     * 添加属性键值
     *
     * @param key 属性键
     * @param val 属性值
     */
    void addProperty(@Nonnull final String key, @Nonnull final Object val);

    /**
     * 获取属性值
     *
     * @param key 属性键
     * @return 属性值
     */
    Object getProperty(@Nonnull final String key);

    /**
     * 获取客户端IP地址。
     *
     * @return 客户端IP地址。
     */
    String getClientIp();

    /**
     * 获取是否可用
     *
     * @return 是否可用
     */
    boolean isActive();

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
     * 异步执行
     *
     * @param handler 执行处理器
     */
    void execute(@Nonnull final Runnable handler);

    /**
     * 关闭会话
     */
    void close();
}
