package top.zenyoung.netty.server.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.config.NettyProperites;
import top.zenyoung.netty.handler.HeartbeatHandler;
import top.zenyoung.netty.handler.IpAddrFilter;
import top.zenyoung.netty.handler.RequestLimitFilter;
import top.zenyoung.netty.handler.SocketHandler;
import top.zenyoung.netty.server.NettyServer;
import top.zenyoung.netty.util.BeanUtils;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * NettyServer服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class NettyServerImpl implements NettyServer {
    private final NettyProperites properites;
    private final ApplicationContext context;

    private static final boolean IS_EPOLL;
    private static final EventLoopGroup BOSS_GROUP;
    private static final EventLoopGroup WORKER_GROUP;

    static {
        final int bossSize = Math.max(Runtime.getRuntime().availableProcessors() * 2, 2);
        final int workSize = bossSize * 2;

        IS_EPOLL = Epoll.isAvailable();
        BOSS_GROUP = IS_EPOLL ? new EpollEventLoopGroup(bossSize) : new NioEventLoopGroup(bossSize);
        WORKER_GROUP = IS_EPOLL ? new EpollEventLoopGroup(workSize) : new NioEventLoopGroup(workSize);
    }


    @Override
    public void run() {
        try {
            log.info("Netty启动...");
            final Integer port = this.properites.getPort();
            if (Objects.isNull(port) || port <= 0) {
                log.error("Netty-未配置服务器监听端口!");
                return;
            }
            //启动
            final ChannelFuture future = this.start(port);
            //jvm钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Netty开始关闭服务...");
                    this.close();
                } catch (Throwable ex) {
                    log.error("Netty关闭异常: {}", ex.getMessage());
                }
            }));
            //同步阻塞
            log.info("Netty启动成功...");
            future.channel().closeFuture().syncUninterruptibly();
        } catch (Throwable e) {
            log.error("Netty运行失败: {}", e.getMessage());
        }
    }

    private LogLevel getLogLevel() {
        final String level;
        if (!Strings.isNullOrEmpty(level = this.properites.getLogLevel())) {
            try {
                return Enum.valueOf(LogLevel.class, level.toUpperCase());
            } catch (Throwable e) {
                log.warn("getLogLevel(level: {})-exp: {}", level, e.getMessage());
            }
        }
        return LogLevel.DEBUG;
    }

    private ChannelFuture start(@Nonnull final Integer port) {
        log.info("Netty启动[port: {}]...", port);
        //心跳间隔
        final Duration heartbeat = this.properites.getHeartbeatInterval();
        //保持连接数
        final int backlog = Math.max(this.properites.getBacklog(), 50);
        //日志级别
        final LogLevel logLevel = this.getLogLevel();
        //初始化启动器
        final ServerBootstrap bootstrap = new ServerBootstrap();
        //设置分组设置
        bootstrap.group(BOSS_GROUP, WORKER_GROUP)
                .channel(IS_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                //保持连接数
                .option(ChannelOption.SO_BACKLOG, backlog)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //保存连接
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //TCP立即发包
                .childOption(ChannelOption.TCP_NODELAY, true)
                //日志
                .handler(new LoggingHandler(logLevel))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) {
                        log.info("Netty新设备连接: {}", ch);
                        //获取通信管道
                        final ChannelPipeline pipeline = ch.pipeline();
                        if (Objects.nonNull(pipeline)) {
                            //1.挂载IP地址过滤器
                            pipeline.addLast("ipFilter", new IpAddrFilter(properites));
                            //2.挂载请求限制过滤器
                            if (Objects.nonNull(properites.getLimit())) {
                                pipeline.addLast("limitFilter", new RequestLimitFilter(properites));
                            }
                            //3.挂载空闲检查处理器
                            if (Objects.nonNull(heartbeat)) {
                                pipeline.addLast("idle", new HeartbeatHandler(heartbeat));
                                log.info("Netty-挂载空闲检查处理器: {}", heartbeat);
                            }
                            //4.挂载编解码器
                            final Map<String, ChannelHandler> codecMaps = BeanUtils.getCodecMap(context, properites);
                            if (!CollectionUtils.isEmpty(codecMaps)) {
                                codecMaps.forEach(pipeline::addLast);
                            }
                            //5.挂载业务处理器
                            final SocketHandler handler = BeanUtils.getBizHandler(context);
                            if (Objects.nonNull(handler)) {
                                pipeline.addLast("biz", handler);
                                log.info("Netty-挂载业务处理器:" + handler);
                            }
                            log.info("已挂载处理器: {}", Joiner.on(",").skipNulls().join(pipeline.names()));
                        }
                    }
                });
        //Epoll设置
        if (IS_EPOLL) {
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
                    .childOption(EpollChannelOption.TCP_QUICKACK, true);
        }
        //绑定端口
        final ChannelFuture future = bootstrap.bind(port);
        future.syncUninterruptibly();
        return future;
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
