package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import top.zenyoung.netty.handler.BaseSocketHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Socket处理器工具类
 *
 * @author young
 */
@Slf4j
public class SocketUtils {

    /**
     * 从Spring中获取通道处理器
     *
     * @param context Spring上下文
     * @param cls     通道处理器类型
     * @return 处理器对象
     */
    @SuppressWarnings({"all"})
    public static ChannelHandler getHandler(@Nonnull final ApplicationContext context,
                                            @Nonnull final Class<? extends BaseSocketHandler> cls) {
        try {
            return context.getBean(cls);
        } catch (Throwable e) {
            log.error("getHandler-exp: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取远端地址
     *
     * @param channel 通道
     * @return 远端地址
     */
    public static InetSocketAddress getRemoteAddr(@Nonnull final Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress());
    }

    /**
     * 获取本地地址
     *
     * @param channel 通道
     * @return 本地地址
     */
    public static InetSocketAddress getLocalAddr(@Nonnull final Channel channel) {
        return ((InetSocketAddress) channel.localAddress());
    }

    /**
     * 根据地址获取IP和端口
     *
     * @param addr    地址
     * @param handler 获取IP和端口
     */
    public static void getIpAddrWithPort(@Nullable final InetSocketAddress addr, @Nonnull final BiConsumer<String, Integer> handler) {
        if (Objects.nonNull(addr)) {
            final String ipAddr = addr.getAddress().getHostAddress();
            final int port = addr.getPort();
            if (!Strings.isNullOrEmpty(ipAddr)) {
                handler.accept(ipAddr, port);
            }
        }
    }

    /**
     * 获取通道ID
     *
     * @param channel 通道
     * @return 通道ID
     */
    public static String getChannelId(@Nonnull final Channel channel) {
        return channel.id().asShortText();
    }

    /**
     * 获取通道ID
     *
     * @param ctx 通道上下文
     * @return 通道ID
     */
    public static String getChannelId(@Nonnull final ChannelHandlerContext ctx) {
        return getChannelId(ctx.channel());
    }

}
