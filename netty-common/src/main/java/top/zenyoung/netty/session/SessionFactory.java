package top.zenyoung.netty.session;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.util.SocketUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Optional;
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
        this.clientIp = Optional.ofNullable(SocketUtils.getRemoteAddr(channel)).map(InetSocketAddress::toString).orElse(null);
        this.setStatus(true);
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

    private void setStatus(final boolean status) {
        this.refStatus.set(status);
    }

    @Override
    public <T> void send(@Nonnull final T content, @Nullable final SendMessageResultListener listener) {
        log.info("发送消息[{}]: {}", getStatus(), content);
        if (getStatus()) {
            Optional.ofNullable(channel.writeAndFlush(content))
                    .ifPresent(future -> Optional.ofNullable(listener)
                            .ifPresent(l -> future.addListener(f -> {
                                        final boolean ret = f.isSuccess();
                                        log.info("发送消息[结果: {}]=> {}", ret, content);
                                        listener.onSendAfter(ret);
                                    })
                            )
                    );
        }
    }

    @Override
    public void close() {
        Optional.ofNullable(channel)
                .filter(ch -> getStatus())
                .ifPresent(ch -> {
                    setStatus(true);
                    Optional.ofNullable(ch.close())
                            .ifPresent(future -> {
                                future.addListener(f -> {
                                    //更新状态
                                    setStatus(!f.isSuccess());
                                    Optional.ofNullable(closeEventListenter)
                                            .filter(listenter -> f.isSuccess())
                                            .ifPresent(listenter -> {
                                                //关闭成功处理
                                                listenter.accept(Info.of(getDeviceId(), getClientIp()));
                                            });
                                });
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
