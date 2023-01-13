package top.zenyoung.netty;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.config.BaseProperties;
import top.zenyoung.netty.util.SocketUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Netty实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseNettyImpl<T extends BaseProperties> implements Runnable, Closeable {
    protected static final boolean IS_EPOLL;
    protected static final EventLoopGroup BOSS_GROUP;
    protected static final EventLoopGroup WORKER_GROUP;

    static {
        final int bossSize = Math.max(Runtime.getRuntime().availableProcessors() * 2, 2);
        final int workSize = bossSize * 2;

        IS_EPOLL = Epoll.isAvailable();
        BOSS_GROUP = IS_EPOLL ? new EpollEventLoopGroup(bossSize) : new NioEventLoopGroup(bossSize);
        WORKER_GROUP = IS_EPOLL ? new EpollEventLoopGroup(workSize) : new NioEventLoopGroup(workSize);
    }

    /**
     * 获取配置数据
     *
     * @return 配置数据
     */
    protected abstract T getProperties();

    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    protected LogLevel getLogLevel() {
        final String level;
        if (!Strings.isNullOrEmpty(level = this.getProperties().getLogLevel())) {
            try {
                return Enum.valueOf(LogLevel.class, level.toUpperCase());
            } catch (Throwable e) {
                log.warn("getLogLevel(level: {})-exp: {}", level, e.getMessage());
            }
        }
        return LogLevel.DEBUG;
    }

    /**
     * 获取保持连接数
     *
     * @return 保持连接数
     */
    protected int getBacklog() {
        return 2048;
    }

    /**
     * 构建Bootstrap参数
     *
     * @param bootstrap      bootstrap对象
     * @param channelHandler 通道处理器
     * @param <C>            通道类型
     * @param <B>            bootstrap对象类型
     */
    protected <B extends AbstractBootstrap<B, C>, C extends Channel> void buildBootstrap(
            @Nonnull final AbstractBootstrap<B, C> bootstrap,
            @Nonnull final Supplier<Class<? extends C>> channelHandler
    ) {
        buildBootstrap(bootstrap, null, null, channelHandler);
    }

    /**
     * 构建Bootstrap参数
     *
     * @param bootstrap      bootstrap对象
     * @param work           工作线程池
     * @param channelHandler 通道处理器
     * @param <C>            通道类型
     * @param <B>            bootstrap对象类型
     */
    protected <B extends AbstractBootstrap<B, C>, C extends Channel> void buildBootstrap(
            @Nonnull final AbstractBootstrap<B, C> bootstrap,
            @Nullable final EventLoopGroup work,
            @Nonnull final Supplier<Class<? extends C>> channelHandler
    ) {
        buildBootstrap(bootstrap, null, work, channelHandler);
    }

    /**
     * 构建Bootstrap参数
     *
     * @param bootstrap      bootstrap对象
     * @param boss           主线程池(服务端)
     * @param work           工作线程池
     * @param channelHandler 通道处理器
     * @param <C>            通道类型
     * @param <B>            bootstrap对象类型
     */
    protected <B extends AbstractBootstrap<B, C>, C extends Channel> void buildBootstrap(
            @Nonnull final AbstractBootstrap<B, C> bootstrap,
            @Nullable final EventLoopGroup boss, @Nullable final EventLoopGroup work,
            @Nonnull final Supplier<Class<? extends C>> channelHandler
    ) {
        //工作线程池
        if (bootstrap instanceof ServerBootstrap) {
            final EventLoopGroup bossGroup = Objects.nonNull(boss) ? boss : BOSS_GROUP;
            final EventLoopGroup workGroup = Objects.nonNull(work) ? work : WORKER_GROUP;
            ((ServerBootstrap) bootstrap).group(bossGroup, workGroup);
        } else {
            bootstrap.group(Objects.nonNull(work) ? work : WORKER_GROUP);
        }
        //channel配置
        bootstrap.channel(channelHandler.get())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        //客户端配置参数
        if (bootstrap instanceof Bootstrap) {
            //保存连接
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    //TCP立即发包
                    .option(ChannelOption.TCP_NODELAY, true);
        }
        //Epoll设置
        if (IS_EPOLL) {
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
        }
        //服务器配置
        if (bootstrap instanceof ServerBootstrap) {
            final ServerBootstrap serverBootstrap = (ServerBootstrap) bootstrap;
            //保持连接数
            final int backlog = Math.max(this.getBacklog(), 50);
            serverBootstrap
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //Epoll设置
            if (IS_EPOLL) {
                serverBootstrap.childOption(EpollChannelOption.TCP_QUICKACK, true);
            }
        }
        //业务处理管道
        final Supplier<ChannelHandler> channelPipelineHandler = () -> new ChannelInitializer<C>() {
            @Override
            protected void initChannel(final C ch) {
                final InetSocketAddress socketAddr = (InetSocketAddress) ch.localAddress();
                final int port = Objects.isNull(socketAddr) ? -1 : socketAddr.getPort();
                if (port > -1) {
                    log.info("Netty[{}]新设备连接: {}", port, ch);
                }
                //获取通信管道
                final ChannelPipeline pipeline = ch.pipeline();
                if (Objects.nonNull(pipeline)) {
                    initChannelPipelineHandler(port, pipeline);
                    log.info("已挂载处理器: {}", Joiner.on(",").skipNulls().join(pipeline.names()));
                }
            }
        };
        //Options处理
        this.addBootstrapOptions(bootstrap);
        //日志处理器
        final LoggingHandler loggingHandler = new LoggingHandler(this.getLogLevel());
        //服务器端
        if (bootstrap instanceof ServerBootstrap) {
            ((ServerBootstrap) bootstrap)
                    .childHandler(loggingHandler)
                    .childHandler(channelPipelineHandler.get());
        } else {
            //客户端
            bootstrap
                    .handler(loggingHandler)
                    .handler(channelPipelineHandler.get());
        }
    }

    protected <B extends AbstractBootstrap<B, C>, C extends Channel> void addBootstrapOptions(@Nonnull final AbstractBootstrap<B, C> bootstrap) {

    }

    /**
     * 通信管道初始化
     *
     * @param port     端口
     * @param pipeline 管道对象
     */
    protected abstract void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline);

    /**
     * 开启异步执行
     *
     * @param ctx     通道上下文
     * @param handler 执行任务
     */
    protected static void execute(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Runnable handler) {
        ctx.executor().execute(handler);
    }

    /**
     * 开启异步执行
     *
     * @param channel 通道对象
     * @param handler 执行任务
     */
    protected static void execute(@Nonnull final Channel channel, @Nonnull final Runnable handler) {
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
    protected static ScheduledFuture<?> scheduleCreate(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Runnable task, @Nonnull final Duration delay) {
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
    protected static <R> ScheduledFuture<R> schedule(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Callable<R> task, @Nonnull final Duration delay) {
        return ctx.executor().schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 取消定时任务
     *
     * @param future 任务句柄
     */
    protected static void scheduleCancel(@Nullable final ScheduledFuture<?> future) {
        if (Objects.nonNull(future)) {
            future.cancel(false);
        }
    }

    /**
     * 通道写入消息数据
     *
     * @param ctx      通道上下文
     * @param msg      消息数据
     * @param listener 通道监听器
     * @param <T>      消息数据类型
     */
    protected static <T> void writeAndFlush(@Nullable final ChannelHandlerContext ctx, @Nonnull final T msg, @Nullable final ChannelFutureListener listener) {
        if (Objects.nonNull(ctx)) {
            final ChannelFuture future = ctx.writeAndFlush(msg);
            if (Objects.nonNull(listener)) {
                future.addListener(listener);
            }
        }
    }

    /**
     * 通道写入消息数据
     *
     * @param channel  通道对象
     * @param msg      消息数据
     * @param listener 通道监听器
     * @param <T>      消息数据类型
     */
    protected static <T> void writeAndFlush(@Nullable final Channel channel, @Nonnull final T msg, @Nullable final ChannelFutureListener listener) {
        if (Objects.nonNull(channel)) {
            final ChannelFuture future = channel.writeAndFlush(msg);
            if (Objects.nonNull(listener)) {
                future.addListener(listener);
            }
        }
    }

    /**
     * 同步阻塞并添加JVM钩子
     *
     * @param futures ChannelFuture
     */
    protected void syncShutdownHook(@Nonnull final ChannelFuture... futures) {
        if (futures.length > 0) {
            try {
                //jvm钩子
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        log.info("netty 开始关闭...");
                        this.close();
                    } catch (Throwable e) {
                        log.error("netty 开始关闭异常-exp: {}", e.getMessage());
                    }
                }));
                //同步阻塞
                Stream.of(futures)
                        .filter(Objects::nonNull)
                        .forEach(f -> {
                            final Channel ch;
                            if (Objects.nonNull(ch = f.channel())) {
                                try {
                                    ch.closeFuture().sync();
                                } catch (Throwable ex) {
                                    log.error("同步阻塞[channelId: {}]异常-exp: {}", SocketUtils.getChannelId(ch), ex.getMessage());
                                }
                            }
                        });
            } catch (Throwable e) {
                log.error("netty 运行失败-exp: {}", e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        try {
            BOSS_GROUP.shutdownGracefully();
            WORKER_GROUP.shutdownGracefully();
            log.info("Netty关闭成功!");
        } catch (Throwable e) {
            log.error("Netty关闭异常: {}", e.getMessage());
        }
    }
}
