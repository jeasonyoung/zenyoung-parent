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
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.config.BaseProperties;
import top.zenyoung.netty.mbean.TrafficAcceptor;
import top.zenyoung.netty.util.SocketUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
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
public abstract class BaseNettyImpl<T extends BaseProperties> implements Runnable {
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
     * 全局流量共享统计处理器
     */
    @Getter
    private GlobalTrafficShapingHandler globalTrafficHandler;

    /**
     * 获取配置数据
     *
     * @return 配置数据
     */
    protected abstract T getProperties();

    /**
     * 获取全局流量统计
     *
     * @return 全局流量统计
     */
    protected TrafficAcceptor getGlobalTraffic() {
        return Optional.ofNullable(globalTrafficHandler)
                .map(GlobalTrafficShapingHandler::trafficCounter)
                .map(TrafficAcceptor::of)
                .orElse(null);
    }

    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    protected LogLevel getLogLevel() {
        return Optional.ofNullable(getProperties().getLogLevel())
                .filter(level -> !Strings.isNullOrEmpty(level))
                .map(level -> createHandler(() -> Enum.valueOf(LogLevel.class, level.toUpperCase())))
                .orElse(LogLevel.DEBUG);
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
        //初始化全局流量处理器
        if (Objects.isNull(globalTrafficHandler)) {
            this.globalTrafficHandler = new GlobalTrafficShapingHandler(WORKER_GROUP, 1000L);
        }
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
        //Options处理
        this.addBootstrapOptions(bootstrap);
        //服务器端
        final BaseNettyImpl<?> impl = this;
        if (bootstrap instanceof ServerBootstrap) {
            ((ServerBootstrap) bootstrap).childHandler(new ChannelInitializer<ServerChannel>() {
                @Override
                protected void initChannel(final ServerChannel ch) {
                    impl.initChannel(ch);
                }
            });
        } else {
            //客户端
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(final Channel ch) {
                    impl.initChannel(ch);
                }
            });
        }
        //启动startMbean处理
        startMbean();
    }

    /**
     * 初始化通道处理
     *
     * @param channel 连接通道
     */
    protected void initChannel(@Nonnull final Channel channel) {
        final Integer port = Optional.ofNullable((InetSocketAddress) channel.localAddress())
                .map(InetSocketAddress::getPort)
                .orElse(-1);
        log.info("Netty[{}]新设备连接: {}", port, SocketUtils.getChannelId(channel));
        //获取通信管道
        Optional.ofNullable(channel.pipeline())
                .ifPresent(pipeline -> {
                    initChannelPipelineHandler(port, pipeline);
                    log.info("已挂载处理器: {}", Joiner.on(",").skipNulls().join(pipeline.names()));
                });
    }

    protected void startMbean() {
        Optional.ofNullable(getGlobalTraffic())
                .ifPresent(mbean -> {
                    try {
                        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
                        final ObjectName acceptorName = new ObjectName(mbean.getClass().getPackage().getName() + ":type=TrafficAcceptor");
                        mBeanServer.registerMBean(mbean, acceptorName);
                    } catch (Throwable e) {
                        log.warn("startMbean[{}]-exp: {}", mbean, e.getMessage());
                    }
                });
    }

    protected <B extends AbstractBootstrap<B, C>, C extends Channel> void addBootstrapOptions(@Nonnull final AbstractBootstrap<B, C> bootstrap) {
        bootstrap.option(ChannelOption.AUTO_READ, false);
    }

    /**
     * 通信管道初始化
     *
     * @param port     端口
     * @param pipeline 管道对象
     */
    protected void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        //0.挂载日志处理器
        pipeline.addLast("log", new LoggingHandler(getLogLevel()));
        //1.挂载流量统计处理器
        Optional.ofNullable(globalTrafficHandler)
                .ifPresent(handler -> pipeline.addLast("globalTraffic", handler));
    }

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
    protected static <R> ScheduledFuture<R> scheduleCreate(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Callable<R> task, @Nonnull final Duration delay) {
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
     * @param data     消息数据
     * @param listener 通道监听器
     */
    protected static void writeAndFlush(@Nonnull final ChannelHandlerContext ctx, @Nonnull final Message data, @Nullable final ChannelFutureListener listener) {
        Optional.ofNullable(ctx.writeAndFlush(data))
                .filter(future -> Objects.nonNull(listener))
                .ifPresent(future -> future.addListener(listener));
    }

    /**
     * 通道写入消息数据
     *
     * @param channel  通道对象
     * @param data     消息数据
     * @param listener 通道监听器
     */
    protected static void writeAndFlush(@Nonnull final Channel channel, @Nonnull final Message data, @Nullable final ChannelFutureListener listener) {
        Optional.ofNullable(channel.writeAndFlush(data))
                .filter(future -> Objects.nonNull(listener))
                .ifPresent(future -> future.addListener(listener));
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

    /**
     * 关闭
     */
    public void close() {
        try {
            BOSS_GROUP.shutdownGracefully();
            WORKER_GROUP.shutdownGracefully();
            log.info("Netty关闭成功!");
        } catch (Throwable e) {
            log.error("Netty关闭异常: {}", e.getMessage());
        }
    }

    /**
     * 创建处理(不抛异常)
     *
     * @param handler 处理逻辑
     * @param <T>     创建对象
     * @return 创建结果
     */
    protected static <T> T createHandler(@Nonnull final InnerThrowableSupplier<T> handler) {
        try {
            return handler.get();
        } catch (Throwable e) {
            log.error("createHandler(handler: {})-exp: {}", handler, e.getMessage());
            return null;
        }
    }

    @FunctionalInterface
    protected interface InnerThrowableSupplier<T> {
        T get() throws Throwable;
    }
}
