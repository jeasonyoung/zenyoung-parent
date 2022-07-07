package top.zenyoung.netty.client.client.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.BaseNettyImpl;
import top.zenyoung.netty.client.client.NettyClient;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.client.handler.BaseClientSocketHandler;
import top.zenyoung.netty.handler.HeartbeatHandler;
import top.zenyoung.netty.util.CodecUtils;
import top.zenyoung.netty.util.SocketUtils;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NettyClient-客户端实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class NettyClientImpl extends BaseNettyImpl<NettyClientProperties> implements NettyClient {
    private final AtomicReference<Future<?>> refReconnect = new AtomicReference<>(null);
    private final NettyClientProperties properites;
    private final ApplicationContext context;
    private Bootstrap bootstrap;

    @Override
    protected NettyClientProperties getProperties() {
        return this.properites;
    }

    @Override
    public final void run() {
        try {
            log.info("Netty启动...");
            final Integer port = this.getProperties().getPort();
            if (Objects.isNull(port) || port <= 0) {
                log.error("Netty-未配置服务器监听端口!");
                return;
            }
            //启动
            this.start(port);
        } catch (Throwable e) {
            log.error("Netty运行失败: {}", e.getMessage());
        }
    }

    protected void start(@Nonnull final Integer port) {
        log.info("Netty启动[port: {}]...", port);
        //心跳间隔
        final Duration heartbeat = this.properites.getHeartbeatInterval();
        //日志级别
        final LogLevel logLevel = this.getLogLevel();
        //创建客户端启动对象
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(WORKER_GROUP)
                .channel(IS_EPOLL ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //保存连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //TCP立即发包
                .option(ChannelOption.TCP_NODELAY, true)
                //日志
                .handler(new LoggingHandler(logLevel))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) {
                        log.info("Netty连接服务器: {}", ch);
                        //获取通信管道
                        final ChannelPipeline pipeline = ch.pipeline();
                        if (Objects.nonNull(pipeline)) {
                            //1.挂载空闲检查处理器
                            if (Objects.nonNull(heartbeat)) {
                                pipeline.addLast("idle", new HeartbeatHandler(heartbeat));
                                log.info("Netty-挂载空闲检查处理器: {}", heartbeat);
                            }
                            //2.挂载编解码器
                            final Map<String, ChannelHandler> codecMaps = CodecUtils.getCodecMap(context, properites.getCodec(), true);
                            if (!CollectionUtils.isEmpty(codecMaps)) {
                                codecMaps.forEach(pipeline::addLast);
                            }
                            //3.挂载业务处理器
                            final ChannelHandler handler = SocketUtils.getHandler(context, BaseClientSocketHandler.class);
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
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
        }
        //jvm钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Netty开始关闭服务...");
                this.closeReconnectTask();
                this.close();
            } catch (Throwable ex) {
                log.error("Netty关闭异常: {}", ex.getMessage());
            }
        }));
        //启动连接
        this.connect();
    }

    private void connect() {
        final String host = this.properites.getServerIp();
        final Integer port = this.properites.getPort();
        log.info("netty client start[{}:{}]...", host, port);
        //检查bootstrap
        if (Strings.isNullOrEmpty(host) || Objects.isNull(port) || Objects.isNull(this.bootstrap)) {
            log.error("[host: {},port: {}]bootstrap is null.", host, port);
            return;
        }
        //重连间隔
        final Duration interval = this.properites.getReconnectInterval();
        //启动客户端去连接服务端
        final ChannelFuture cf = this.bootstrap.connect(host, port);
        cf.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                log.info("连接服务器[{}:{}]-连接成功", host, port);
                this.closeReconnectTask();
                return;
            }
            //重连交给后端线程执行
            if (Objects.isNull(refReconnect.get())) {
                final Future<?> reconnect = f.channel().eventLoop().schedule(() -> {
                    try {
                        connect();
                    } catch (Throwable e) {
                        log.error("重连服务器[{}:{}]-连接失败: {}", host, port, e.getMessage());
                    }
                }, interval.toMillis(), TimeUnit.MILLISECONDS);
                refReconnect.set(reconnect);
            }
        });
        cf.syncUninterruptibly();
        cf.channel().closeFuture().syncUninterruptibly();
    }

    private void closeReconnectTask() {
        final Future<?> reconnect;
        if (Objects.nonNull(reconnect = refReconnect.get())) {
            reconnect.cancel(false);
            refReconnect.set(null);
        }
    }
}
