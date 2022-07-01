package top.zenyoung.netty.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Socket通讯会话工厂
 *
 * @author young
 */
@Slf4j
public class SessionFactory implements Session {
    private final Channel channel;
    private final String deviceId, clientIp;
    private final AtomicBoolean refStatus = new AtomicBoolean(false);

    private SessionFactory(@Nonnull final Channel channel, @Nonnull final String deviceId) {
        this.channel = channel;
        this.deviceId = deviceId;
        this.clientIp = channel.remoteAddress() + "";
        this.refStatus.set(true);
    }

    /**
     * 创建Socket会话
     *
     * @param channel  会话通道
     * @param deviceId 设备ID
     * @return Socket会话
     */
    public static Session create(@Nonnull final Channel channel, @Nonnull final String deviceId) {
        return new SessionFactory(channel, deviceId);
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    @Override
    public String getDeviceId() {
        return this.deviceId;
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    @Override
    public String getClientIp() {
        return this.clientIp;
    }

    /**
     * 获取会话状态
     *
     * @return 会话状态
     */
    @Override
    public boolean getStatus() {
        return this.refStatus.get();
    }

    @Override
    public <T> void send(@Nonnull final T content, @Nullable final SendMessageResultListener listener) {
        log.info("发送消息: {}", content);
        //发送消息
        final ChannelFuture future = this.channel.writeAndFlush(content);
        if (Objects.nonNull(future) && Objects.nonNull(listener)) {
            future.addListener(f -> {
                final boolean ret = f.isSuccess();
                log.info("发送消息[结果: {}]=> {}", ret, content);
                listener.onSendAfter(ret);
            });
        }
    }

    @Override
    public void close() {
        if (Objects.nonNull(this.channel) && this.getStatus()) {
            log.info("关闭通道:[{}]{}, {}", this.deviceId, this.clientIp, this.channel);
            final ChannelFuture future = channel.close();
            if (Objects.nonNull(future)) {
                future.addListener(f -> {
                    this.refStatus.set(!f.isSuccess());
                });
            }
        }
    }

    @Override
    public String toString() {
        return String.format("deviceId: %1$s,clientIp: %2$s,channel: %3$s", this.deviceId, this.clientIp, this.channel);
    }
}
