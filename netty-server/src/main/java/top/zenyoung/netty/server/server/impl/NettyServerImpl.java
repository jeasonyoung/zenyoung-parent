package top.zenyoung.netty.server.server.impl;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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
import top.zenyoung.netty.util.SocketUtils;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * NettyServer服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class NettyServerImpl extends BaseNettyImpl<NettyServerProperties> implements NettyServer {
    private final NettyServerProperties properites;
    private final ApplicationContext context;

    @Override
    protected NettyServerProperties getProperties() {
        return this.properites;
    }

    @Override
    public void run() {
        try {
            log.info("Netty启动...");
            final Map<Integer, Map<String, String>> portCodecMap = this.properites.getCodec();
            if (CollectionUtils.isEmpty(portCodecMap)) {
                log.error("Netty-未配置服务器监听端口及编解码器!");
                return;
            }
            //启动
            final List<ChannelFuture> futures = this.start(portCodecMap);
            if (!CollectionUtils.isEmpty(futures)) {
                //jvm钩子
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        log.info("Netty开始关闭服务...");
                        this.close();
                    } catch (Throwable ex) {
                        log.error("Netty关闭异常: {}", ex.getMessage());
                    }
                }));
                log.info("Netty启动成功...");
                //同步阻塞
                futures.forEach(f -> f.channel().closeFuture().syncUninterruptibly());
            }
        } catch (Throwable e) {
            log.error("Netty运行失败: {}", e.getMessage());
        }
    }

    protected List<ChannelFuture> start(@Nonnull final Map<Integer, Map<String, String>> portCodecMap) {
        final List<Integer> ports = Lists.newArrayList(portCodecMap.keySet());
        if (CollectionUtils.isEmpty(ports)) {
            log.error("未配置监听端口,请检查配置!");
            return null;
        }
        log.info("Netty启动: {}", ports);
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
                        final InetSocketAddress socketAddr = ch.localAddress();
                        final int port = socketAddr.getPort();
                        log.info("Netty[{}]新设备连接: {}", port, ch);
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
                            }
                            //4.挂载编解码器
                            final Map<String, ChannelHandler> codecHandlerMap = CodecUtils.getCodecMap(context, portCodecMap.get(port), true);
                            if (CollectionUtils.isEmpty(codecHandlerMap)) {
                                log.error("端口[{}]未挂载编解码器,请检查配置!", port);
                            } else {
                                codecHandlerMap.forEach(pipeline::addLast);
                                log.info("端口[{}]挂载编解码器: {}", port, codecHandlerMap.keySet());
                            }
                            //5.挂载业务处理器
                            final ChannelHandler handler = SocketUtils.getHandler(context, BaseServerSocketHandler.class);
                            if (Objects.nonNull(handler)) {
                                pipeline.addLast("biz", handler);
                            }
                            log.info("端口[{}]已挂载处理器: {}", port, pipeline.toMap());
                        }
                    }
                });
        //Epoll设置
        if (IS_EPOLL) {
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
                    .childOption(EpollChannelOption.TCP_QUICKACK, true);
        }
        //绑定端口
        return ports.stream()
                .filter(port -> Objects.nonNull(port) && port > 0)
                .map(port -> {
                    final ChannelFuture future = bootstrap.bind(port);
                    future.addListener((ChannelFutureListener) f -> log.info("端口[{}]绑定: {}", port, f.isSuccess() ? "成功" : "失败"));
                    future.syncUninterruptibly();
                    return future;
                })
                .collect(Collectors.toList());
    }
}
