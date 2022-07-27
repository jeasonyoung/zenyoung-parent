package top.zenyoung.netty.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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

    private final Consumer<Info> closeEventListenter;

    private SessionFactory(@Nonnull final Channel channel, @Nonnull final String deviceId, @Nullable final Consumer<Info> closeEventListenter) {
        this.channel = channel;
        this.deviceId = deviceId;
        this.clientIp = channel.remoteAddress() + "";
        this.refStatus.set(true);
        this.closeEventListenter = closeEventListenter;
    }

    /**
     * 创建Socket会话
     *
     * @param channel             会话通道
     * @param deviceId            设备ID
     * @param closeEventListenter 关闭事件监听器
     * @return Socket会话
     */
    public static Session create(@Nonnull final Channel channel, @Nonnull final String deviceId, @Nullable final Consumer<Info> closeEventListenter) {
        return new SessionFactory(channel, deviceId, closeEventListenter);
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
        log.info("发送消息[{}]: {}", this.getStatus(), content);
        if (this.getStatus()) {
            try {
                //发送消息
                final ChannelFuture future = this.channel.writeAndFlush(content);
                if (Objects.nonNull(future) && Objects.nonNull(listener)) {
                    future.addListener(f -> {
                        final boolean ret = f.isSuccess();
                        log.info("发送消息[结果: {}]=> {}", ret, content);
                        listener.onSendAfter(ret);
                    });
                }
            } catch (Throwable e) {
                log.error("send(content: {},listener: {})-exp: {}", content, listener, e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        if (Objects.nonNull(this.channel) && this.getStatus()) {
            this.refStatus.set(true);
            log.info("关闭通道:[{}]{}, {}", this.deviceId, this.clientIp, this.channel);
            final ChannelFuture future = channel.close();
            if (Objects.nonNull(future)) {
                future.addListener(f -> {
                    //更新状态
                    this.refStatus.set(!f.isSuccess());
                    //关闭成功处理
                    if (Objects.nonNull(this.closeEventListenter) && f.isSuccess()) {
                        this.closeEventListenter.accept(Info.of(this.deviceId, this.clientIp));
                    }
                });
            }
        }
    }

    @Override
    public String toString() {
        return String.format("deviceId: %1$s,clientIp: %2$s,channel: %3$s", this.deviceId, this.clientIp, this.channel);
    }

    @Data
    @RequiredArgsConstructor(staticName = "of")
    public static class Info implements Serializable {
        /**
         * 设备ID
         */
        private final String deviceId;
        /**
         * IP地址
         */
        private final String clientIp;
    }
}
