package top.zenyoung.netty.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.util.NettyUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Socket通讯会话工厂
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class SessionFactory implements Session {
    private final Channel channel;
    private final String deviceId;

    @Override
    public <T> Attribute<T> attr(@Nonnull final AttributeKey<T> key) {
        return channel.attr(key);
    }

    @Override
    public <T> boolean hasAttr(@Nonnull final AttributeKey<T> key) {
        return channel.hasAttr(key);
    }

    @Override
    public String getChannelId() {
        return NettyUtils.getChannelId(channel);
    }

    @Override
    public String getDeviceId() {
        return this.deviceId;
    }

    @Override
    public String getClientIp() {
        return Optional.ofNullable(channel)
                .map(NettyUtils::getRemoteAddr)
                .map(NettyUtils::getIpAddr)
                .orElse(null);
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
                });
    }

    @Override
    public void close() {
        Optional.ofNullable(channel)
                .filter(Channel::isActive)
                .ifPresent(Channel::close);
    }

    @Override
    public String toString() {
        return "session(channelId: " + getChannelId() + ",deviceId:" + getDeviceId() + ",clientIp:" + getClientIp() + ")";
    }
}
