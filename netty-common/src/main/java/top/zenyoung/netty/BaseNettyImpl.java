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
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.config.BaseProperties;
import top.zenyoung.netty.util.SocketUtils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.Objects;
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
     * @param bootstrap bootstrap对象
     */
    protected <B extends AbstractBootstrap<B, C>, C extends Channel> void buildBootstrap(
            @Nonnull final AbstractBootstrap<B, C> bootstrap,
            @Nonnull final Supplier<Class<? extends C>> channelHandler
    ) {
        //工作线程池
        if (bootstrap instanceof ServerBootstrap) {
            ((ServerBootstrap) bootstrap).group(BOSS_GROUP, WORKER_GROUP);
        } else {
            bootstrap.group(WORKER_GROUP);
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

    /**
     * 通信管道初始化
     *
     * @param port     端口
     * @param pipeline 管道对象
     */
    protected abstract void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline);

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
