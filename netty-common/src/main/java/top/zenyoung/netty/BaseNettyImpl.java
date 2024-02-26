package top.zenyoung.netty;

import com.google.common.base.Joiner;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.netty.config.BaseProperties;
import top.zenyoung.netty.handler.HeartbeatHandler;
import top.zenyoung.netty.handler.IpAddrFilterHandler;
import top.zenyoung.netty.mbean.TrafficAcceptor;
import top.zenyoung.netty.util.NettyUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Netty实现基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseNettyImpl extends ChannelInboundHandlerAdapter {
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
    private GlobalTrafficShapingHandler globalTrafficHandler = null;

    /**
     * 获取配置数据
     *
     * @return 配置数据
     */
    protected abstract BaseProperties getProperties();

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
    protected LogLevel getNettyLogLevel() {
        return Optional.ofNullable(getProperties())
                .map(BaseProperties::getNettyLogLevel)
                .orElse(LogLevel.INFO);
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
     * @param bootstrap 服务端
     */
    protected void buildBootstrap(@Nonnull final ServerBootstrap bootstrap) {
        buildBootstrap(bootstrap, () -> IS_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
    }

    /**
     * 构建Bootstrap参数
     *
     * @param bootstrap 客户端
     */
    protected void buildBootstrap(@Nonnull final Bootstrap bootstrap) {
        buildBootstrap(bootstrap, () -> IS_EPOLL ? EpollSocketChannel.class : NioSocketChannel.class);
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
        if (bootstrap instanceof ServerBootstrap srv) {
            final EventLoopGroup bossGroup = Objects.nonNull(boss) ? boss : BOSS_GROUP;
            final EventLoopGroup workGroup = Objects.nonNull(work) ? work : WORKER_GROUP;
            srv.group(bossGroup, workGroup);
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
        if (bootstrap instanceof ServerBootstrap serverBootstrap) {
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
        final BaseNettyImpl impl = this;
        if (bootstrap instanceof ServerBootstrap srv) {
            srv.childHandler(new ChannelInitializer<C>() {

                @Override
                protected void initChannel(final C ch) {
                    impl.initChannel(ch);
                }
            });
        } else {
            //客户端
            bootstrap.handler(new ChannelInitializer<C>() {
                @Override
                protected void initChannel(final C ch) {
                    impl.initChannel(ch);
                }
            });
        }
    }

    /**
     * 初始化通道处理
     *
     * @param channel 连接通道
     */
    protected final void initChannel(@Nonnull final Channel channel) {
        final Integer port = Optional.ofNullable((InetSocketAddress) channel.localAddress())
                .map(InetSocketAddress::getPort)
                .orElse(-1);
        log.info("Netty[{}]新设备连接: {}", port, NettyUtils.getChannelId(channel));
        //获取通信管道
        final ChannelPipeline pipeline = channel.pipeline();
        initChannelPipelineHandler(port, pipeline);
        log.info("已挂载处理器: {}", Joiner.on(",").skipNulls().join(pipeline.names()));
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
        if (bootstrap instanceof ServerBootstrap srv) {
            srv.childOption(ChannelOption.AUTO_READ, false);
        } else {
            bootstrap.option(ChannelOption.AUTO_READ, false);
        }
    }

    /**
     * 通信管道初始化
     *
     * @param port     端口
     * @param pipeline 管道对象
     */
    protected final void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        //ip过滤
        pipeline.addLast("ipFilter", new IpAddrFilterHandler(getProperties()));
        //ssl
        final SslHandler sslHandler = buildSslHandle(port, pipeline.channel());
        if (Objects.nonNull(sslHandler)) {
            pipeline.addLast("ssl", sslHandler);
        }
        //挂载流量统计
        Optional.ofNullable(globalTrafficHandler)
                .ifPresent(handler -> pipeline.addLast("globalTraffic", handler));
        //挂载编解码器
        initChannelCodecHandler(port, pipeline);
        //日志处理
        pipeline.addLast("log", new LoggingHandler(getNettyLogLevel()));
        //空闲检测器
        Optional.ofNullable(getProperties())
                .map(BaseProperties::getHeartbeatInterval)
                .filter(duration -> !duration.isZero())
                .ifPresent(heartbeat -> pipeline.addLast("idle", new HeartbeatHandler(heartbeat)));
        //业务处理器
        initBizHandlers(port, pipeline);
    }

    /**
     * 获取ssl处理器
     *
     * @param port 处理端口
     * @return ssl处理器
     */
    @Nullable
    protected SslHandler buildSslHandle(final int port, @Nonnull final Channel channel) {
        return null;
    }

    /**
     * 添加通道编解码器
     *
     * @param port     端口
     * @param pipeline 通道线
     */
    protected abstract void initChannelCodecHandler(final int port, @Nonnull final ChannelPipeline pipeline);

    /**
     * 添加业务处理器集合
     *
     * @param port     端口
     * @param pipeline 通道线
     */
    protected abstract void initBizHandlers(final int port, @Nonnull final ChannelPipeline pipeline);

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
}
