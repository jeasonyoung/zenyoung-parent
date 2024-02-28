package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjIntConsumer;
import java.util.regex.Pattern;

/**
 * Socket处理器工具类
 *
 * @author young
 */
@UtilityClass
public class NettyUtils {
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
     * 获取IP地址
     *
     * @param addr InetSocketAddress
     * @return IP地址
     */
    public static String getIpAddr(@Nullable final InetSocketAddress addr) {
        return Optional.ofNullable(addr)
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .orElse(null);
    }

    /**
     * 获取端口号
     *
     * @param addr InetSocketAddress
     * @return 端口号
     */
    public static Integer getIpPort(@Nullable final InetSocketAddress addr) {
        return Optional.ofNullable(addr)
                .map(InetSocketAddress::getPort)
                .orElse(null);
    }

    /**
     * 根据地址获取IP和端口
     *
     * @param addr    地址
     * @param handler 获取IP和端口
     */
    public static void getIpAddrWithPort(@Nullable final InetSocketAddress addr,
                                         @Nonnull final ObjIntConsumer<String> handler) {
        Optional.ofNullable(addr)
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .filter(ipAddr -> !Strings.isNullOrEmpty(ipAddr))
                .ifPresent(ipAddr -> {
                    //端口
                    final Integer port = Optional.of(addr)
                            .map(InetSocketAddress::getPort)
                            .orElse(null);
                    handler.accept(ipAddr, port);
                });
    }

    /**
     * 获取通道ID
     *
     * @param channel 通道
     * @return 通道ID
     */
    public static String getChannelId(@Nullable final Channel channel) {
        return Optional.ofNullable(channel)
                .map(Channel::id)
                .map(ChannelId::asShortText)
                .orElse(null);
    }

    /**
     * 获取通道ID
     *
     * @param ctx 通道上下文
     * @return 通道ID
     */
    public static String getChannelId(@Nullable final ChannelHandlerContext ctx) {
        return Optional.ofNullable(ctx)
                .map(ChannelHandlerContext::channel)
                .map(NettyUtils::getChannelId)
                .orElse(null);
    }

    /**
     * 匹配正则表达式
     *
     * @param patterns 正则表达式集合
     * @param value    匹配数据
     * @return 匹配结果
     */
    public static boolean match(@Nonnull final List<String> patterns, @Nonnull final String value) {
        return Optional.of(patterns)
                .map(regexs -> regexs.stream()
                        .anyMatch(regex -> !Strings.isNullOrEmpty(regex) && Pattern.matches(regex, value))
                )
                .orElse(false);
    }

    /**
     * 检查是否为SSL
     *
     * @param port 端口
     * @return 是否为SSL
     */
    public static boolean isSsl(@Nonnull final Integer port) {
        return port == 443 || port == 8443;
    }

    /**
     * 关闭连接通道
     *
     * @param ch 连接通道
     */
    public static void closeOnFlush(@Nullable final Channel ch) {
        closeOnFlush(ch, null);
    }

    /**
     * 关闭连接通道
     *
     * @param ch       连接通道
     * @param listener 关闭成功处理器
     */
    public static void closeOnFlush(@Nullable final Channel ch,
                                    @Nullable final ChannelFutureListener listener) {
        if (Objects.nonNull(ch) && ch.isActive()) {
            final ChannelFuture future = ch.close();
            if (Objects.nonNull(future) && Objects.nonNull(listener)) {
                future.addListener(listener);
            }
        }
    }

    /**
     * 关闭连接通道
     *
     * @param ctx 连接上下文
     */
    public static void closeOnFlush(@Nullable final ChannelHandlerContext ctx) {
        closeOnFlush(ctx, null);
    }

    /**
     * 关闭连接通道
     *
     * @param ctx      连接上下文
     * @param listener 关闭成功处理器
     */
    public static void closeOnFlush(@Nullable final ChannelHandlerContext ctx,
                                    @Nullable final ChannelFutureListener listener) {
        Optional.ofNullable(ctx)
                .map(ChannelHandlerContext::channel)
                .ifPresent(ch -> closeOnFlush(ch, listener));
    }

    /**
     * 发送消息数据
     *
     * @param channel  通道
     * @param data     消息数据
     * @param listener 通道监听器
     * @param <T>      消息数据类型
     */
    public static <T> void writeAndFlush(@Nullable final Channel channel, @Nullable final T data,
                                         @Nullable final ChannelFutureListener listener) {
        if (Objects.nonNull(channel) && Objects.nonNull(data)) {
            try {
                final ChannelFuture future = channel.writeAndFlush(data);
                if (Objects.nonNull(listener)) {
                    future.addListener(listener);
                }
            } finally {
                if (!channel.config().isAutoRead()) {
                    channel.read();
                }
            }
        }
    }

    /**
     * 发送消息数据
     *
     * @param channel 通道
     * @param data    消息数据
     * @param <T>     消息数据类型
     */
    public static <T> void writeAndFlush(@Nullable final Channel channel, @Nullable final T data) {
        writeAndFlush(channel, data, null);
    }

    /**
     * 发送消息数据
     *
     * @param ctx      通道上下文
     * @param data     消息数据
     * @param listener 发送消息事件处理器
     * @param <T>      消息数据事件处理
     */
    public static <T> void writeAndFlush(@Nullable final ChannelHandlerContext ctx, @Nullable final T data,
                                         @Nullable final ChannelFutureListener listener) {
        Optional.ofNullable(ctx)
                .map(ChannelHandlerContext::channel)
                .ifPresent(ch -> writeAndFlush(ch, data, listener));
    }

    /**
     * 发送消息数据
     *
     * @param ctx  通道上下文
     * @param data 消息数据
     * @param <T>  消息数据类型
     */
    public static <T> void writeAndFlush(@Nullable final ChannelHandlerContext ctx, @Nullable final T data) {
        Optional.ofNullable(ctx)
                .map(ChannelHandlerContext::channel)
                .ifPresent(ch -> writeAndFlush(ch, data, null));
    }

    /**
     * 获取通道失败消息
     *
     * @param future 通道future
     * @return 失败消息
     */
    public static String failMessage(@Nullable final Future<?> future) {
        return Optional.ofNullable(future)
                .map(Future::cause)
                .map(Throwable::getMessage)
                .orElse("");
    }

    /**
     * 开启异步执行
     *
     * @param ctx     通道上下文
     * @param handler 执行任务
     */
    public static void execute(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Runnable handler) {
        ctx.executor().execute(handler);
    }

    /**
     * 开启异步执行
     *
     * @param channel 通道对象
     * @param handler 执行任务
     */
    public static void execute(@Nonnull final Channel channel, @Nonnull final Runnable handler) {
        channel.eventLoop().execute(handler);
    }

    /**
     * 创建定时任务(无返回值)
     *
     * @param ctx   通道上下文
     * @param task  定时任务
     * @param delay 定时间隔
     * @return 任务句柄
     */
    public static ScheduledFuture<?> scheduleCreate(@Nonnull final ChannelHandlerContext ctx,
                                                    @Nonnull final Runnable task, @Nonnull final Duration delay) {
        return ctx.executor().schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 创建定时任务(有返回值)
     *
     * @param ctx   通道上下文
     * @param task  定时任务
     * @param delay 定时间隔
     * @return 任务句柄
     */
    public static <R> ScheduledFuture<R> scheduleCreate(@Nonnull final ChannelHandlerContext ctx,
                                                        @Nonnull final Callable<R> task,
                                                        @Nonnull final Duration delay) {
        return ctx.executor().schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 取消定时任务
     *
     * @param future 任务句柄
     */
    public static void scheduleCancel(@Nullable final ScheduledFuture<?> future) {
        if (Objects.nonNull(future)) {
            future.cancel(false);
        }
    }

    /**
     * 获取异常栈信息打印
     *
     * @param cause 异常类
     * @return 栈信息打印
     */
    public static String getPrintStackTrace(@Nonnull final Throwable cause) {
        final StringWriter sw = new StringWriter();
        try (final PrintWriter printWriter = new PrintWriter(sw)) {
            cause.printStackTrace(printWriter);
            printWriter.flush();
        }
        return sw.toString();
    }
}
