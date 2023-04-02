package top.zenyoung.netty.server.server.impl;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.BaseNettyImpl;
import top.zenyoung.netty.handler.HeartbeatHandler;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.handler.BaseServerSocketHandler;
import top.zenyoung.netty.server.handler.IpAddrFilter;
import top.zenyoung.netty.server.handler.RequestLimitFilter;
import top.zenyoung.netty.server.server.NettyServer;
import top.zenyoung.netty.util.CodecUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * NettyServer服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class NettyServerImpl extends BaseNettyImpl<NettyServerProperties> implements NettyServer {
    private final AtomicBoolean refRun = new AtomicBoolean(false);
    private final NettyServerProperties properites;
    private final ApplicationContext context;

    private final static ScheduledExecutorService EXECUTORS;

    static {
        final int cpus = Runtime.getRuntime().availableProcessors();
        final ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("netty-server-%d").setDaemon(true).build();
        final RejectedExecutionHandler rejected = new ThreadPoolExecutor.DiscardOldestPolicy();
        EXECUTORS = new ScheduledThreadPoolExecutor((int) Math.max(1, cpus * 2), factory, rejected);
    }


    @Override
    protected NettyServerProperties getProperties() {
        return this.properites;
    }

    private Map<Integer, Map<String, String>> portCodec() {
        return Optional.ofNullable(getProperties().getCodec()).orElse(Maps.newHashMap());
    }

    @Override
    protected int getBacklog() {
        return Math.max(this.properites.getBacklog(), 50);
    }

    @Override
    public void run() {
        if (refRun.get()) {
            log.warn("Netty-Server 正在启动或已启动,请确认后操作...");
            return;
        }
        refRun.set(true);
        EXECUTORS.execute(() -> {
            log.info("Netty-Server 准备异步启动...");
            start();
        });
    }

    private void start() {
        try {
            log.info("Netty-Server 启动...");
            final Map<Integer, Map<String, String>> portCodecMap = portCodec();
            if (CollectionUtils.isEmpty(portCodecMap)) {
                log.error("Netty-Server-未配置服务器监听端口及编解码器!");
                return;
            }
            //启动
            final ServerBootstrap bootstrap = new ServerBootstrap();
            buildBootstrap(bootstrap, () -> IS_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
            final List<Integer> ports = portCodecMap.keySet().stream()
                    .filter(port -> Objects.nonNull(port) && port > 0)
                    .collect(Collectors.toList());
            log.info("Netty-Server 监听端口: {}", ports);
            final ChannelFuture[] futures = ports.stream()
                    .map(port -> {
                        final ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
                        future.addListener((ChannelFutureListener) f -> log.info("开始监听端口[{}]: {}", port, f.isSuccess() ? "成功" : "失败"));
                        return future;
                    })
                    .toArray(ChannelFuture[]::new);
            //JVM钩子及同步阻塞
            this.syncShutdownHook(futures);
        } catch (Throwable e) {
            log.error("Netty-Server 运行失败: {}", e.getMessage());
        } finally {
            refRun.set(false);
        }
    }

    @Override
    protected void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        //1.挂载IP地址过滤器
        pipeline.addLast("ipFilter", new IpAddrFilter(properites));
        //2.挂载请求限制过滤器
        Optional.ofNullable(getProperties())
                .map(NettyServerProperties::getLimit)
                .ifPresent(limit -> pipeline.addLast("limitFilter", new RequestLimitFilter(properites)));
        //3.挂载空闲检查处理器
        Optional.ofNullable(getProperties())
                .map(NettyServerProperties::getHeartbeatInterval)
                .ifPresent(heartbeat -> pipeline.addLast("idle", new HeartbeatHandler(heartbeat)));
        //4.挂载编解码器
        final Map<String, String> codecMap = Optional.ofNullable(portCodec())
                .map(map -> map.getOrDefault(port, null))
                .orElse(null);
        final Map<String, ChannelHandler> codecHandlerMap = Optional.ofNullable(context)
                .map(ctx -> CodecUtils.getCodecMap(ctx, codecMap, true))
                .orElse(null);
        if (CollectionUtils.isEmpty(codecHandlerMap)) {
            log.error("端口[{}]未挂载编解码器,请检查配置!", port);
        } else {
            codecHandlerMap.forEach(pipeline::addLast);
            log.info("端口[{}]挂载编解码器: {}", port, codecHandlerMap.keySet());
        }
        //5.挂载业务处理器
        Optional.ofNullable(context)
                .map(ctx -> createHandler(() -> ctx.getBean(BaseServerSocketHandler.class)))
                .ifPresent(handler -> pipeline.addLast("biz", handler));
    }
}
