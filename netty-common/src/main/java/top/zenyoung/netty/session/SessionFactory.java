package top.zenyoung.netty.session;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.util.NettyUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Socket通讯会话工厂
 *
 * @author young
 */
@Slf4j
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
public class SessionFactory implements Session {
    @EqualsAndHashCode.Exclude
    private final Channel channel;
    private final String deviceId;
    private final Map<String, Object> locks = Maps.newHashMap();
    private final Map<String, Object> properties = Maps.newHashMap();

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
    public void addProperty(@Nonnull final String key, @Nonnull final Object val) {
        Assert.hasText(key, "'key'不能为空");
        synchronized (locks.computeIfAbsent(key, k -> new Object())) {
            try {
                properties.put(key, val);
            } finally {
                locks.remove(key);
            }
        }
    }

    @Override
    public Object getProperty(@Nonnull final String key) {
        Assert.hasText(key, "'key'不能为空");
        return properties.getOrDefault(key, null);
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
    public void send(@Nonnull final Object data, @Nullable final ChannelFutureListener listener) {
        Optional.ofNullable(channel)
                .map(ch -> ch.writeAndFlush(data))
                .ifPresent(future -> {
                    if (Objects.nonNull(listener)) {
                        future.addListener(listener);
                    }
                });
    }

    @Override
    public void execute(@Nonnull final Runnable handler) {
        Optional.ofNullable(channel)
                .ifPresent(ch -> NettyUtils.execute(ch, handler));
    }

    @Override
    public void close() {
        Optional.ofNullable(channel)
                .filter(Channel::isActive)
                .ifPresent(Channel::close);
    }
}
