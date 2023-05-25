package top.zenyoung.netty.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.util.NettyUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
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

    private final Consumer<Info> closeEventListenter;

    private SessionFactory(@Nonnull final Channel channel, @Nonnull final String deviceId, @Nullable final Consumer<Info> closeEventListenter) {
        this.channel = channel;
        this.deviceId = deviceId;
        this.clientIp = Optional.ofNullable(NettyUtils.getRemoteAddr(channel)).map(InetSocketAddress::toString).orElse(null);
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

    @Override
    public <T> Attribute<T> attr(@Nonnull final AttributeKey<T> key) {
        return channel.attr(key);
    }

    @Override
    public <T> boolean hasAttr(@Nonnull final AttributeKey<T> key) {
        return channel.hasAttr(key);
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

    @Override
    public boolean isActive() {
        return Optional.ofNullable(channel)
                .map(Channel::isActive)
                .orElse(false);
    }

    @Override
    public final void readChannelData() {
        Optional.ofNullable(channel)
                .ifPresent(Channel::read);
    }

    @Override
    public void send(@Nonnull final Message data, @Nullable final ChannelFutureListener listener) {
        Optional.ofNullable(channel)
                .map(ch -> ch.writeAndFlush(data))
                .ifPresent(future -> {
                    if (Objects.nonNull(listener)) {
                        future.addListener(listener);
                    }
                    future.addListener(f -> {
                        log.info("send(data: {}) => {}", data, f.isSuccess());
                    });
                });
    }

    @Override
    public void close() {
        Optional.ofNullable(channel)
                .filter(Channel::isActive)
                .map(Channel::close)
                .ifPresent(future -> {
                    future.addListener(f -> {
                        if (f.isSuccess()) {
                            //关闭后通知
                            if (Objects.nonNull(closeEventListenter)) {
                                closeEventListenter.accept(Info.of(getDeviceId(), getClientIp()));
                            }
                        }
                    });
                });
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
