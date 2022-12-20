package top.zenyoung.netty.client.client.impl;

import com.google.common.base.Strings;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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

    @Override
    protected NettyClientProperties getProperties() {
        return this.properites;
    }

    @Override
    public final void run() {
        try {
            log.info("Netty启动...");
            //创建客户端启动对象
            final Bootstrap bootstrap = new Bootstrap();
            //构建Bootstrap配置
            this.buildBootstrap(bootstrap, () -> IS_EPOLL ? EpollSocketChannel.class : NioSocketChannel.class);
            //启动连接
            this.connect(bootstrap);
        } catch (Throwable e) {
            log.error("Netty运行失败: {}", e.getMessage());
        }
    }

    @Override
    protected void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        //1.挂载空闲检查处理器
        final Duration heartbeat = this.properites.getHeartbeatInterval();
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
    }

    private void connect(@Nonnull final Bootstrap bootstrap) {
        final String host = this.properites.getHost();
        final Integer port = this.properites.getPort();
        log.info("netty start[{}:{}]...", host, port);
        if (Strings.isNullOrEmpty(host) || Objects.isNull(port)) {
            log.error("未配置服务器: [host: {},port: {}]", host, port);
            return;
        }
        //重连间隔
        final Duration interval = this.properites.getReconnectInterval();
        //启动客户端去连接服务端
        final ChannelFuture future = bootstrap.connect(host, port).syncUninterruptibly();
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                log.info("连接服务器[{}:{}]-连接成功", host, port);
                this.closeReconnectTask();
                return;
            }
            //重连交给后端线程执行
            if (Objects.isNull(refReconnect.get())) {
                final Future<?> reconnect = f.channel().eventLoop().schedule(() -> {
                    try {
                        connect(bootstrap);
                    } catch (Throwable e) {
                        log.error("重连服务器[{}:{}]-连接失败: {}", host, port, e.getMessage());
                    }
                }, interval.toMillis(), TimeUnit.MILLISECONDS);
                refReconnect.set(reconnect);
            }
        });
        //添加钩子并同步阻塞
        this.syncShutdownHook(future);
    }

    private void closeReconnectTask() {
        final Future<?> reconnect;
        if (Objects.nonNull(reconnect = refReconnect.get())) {
            reconnect.cancel(false);
            refReconnect.set(null);
        }
    }

    @Override
    public void close() {
        this.closeReconnectTask();
        super.close();
    }
}
