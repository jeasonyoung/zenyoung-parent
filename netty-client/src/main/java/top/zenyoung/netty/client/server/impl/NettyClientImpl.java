package top.zenyoung.netty.client.server.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.BaseNettyImpl;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.client.handler.BaseClientSocketHandler;
import top.zenyoung.netty.client.handler.ConnectedHandler;
import top.zenyoung.netty.client.handler.PreStartHandler;
import top.zenyoung.netty.client.server.NettyClient;
import top.zenyoung.netty.handler.HeartbeatHandler;
import top.zenyoung.netty.util.ScopeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NettyClient-客户端实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NettyClientImpl extends BaseNettyImpl<NettyClientProperties> implements NettyClient, DisposableBean {
    private final NettyClientProperties properites;
    private final ApplicationContext context;

    private final AtomicBoolean refConnect = new AtomicBoolean(false);
    private final AtomicBoolean refReconnectRun = new AtomicBoolean(false);
    private final AtomicLong refReconnectCount = new AtomicLong(0L);
    private final AtomicReference<ScheduledFuture<?>> refReconnectScheduled = new AtomicReference<>(null);

    private Bootstrap bootstrap;

    @Override
    protected NettyClientProperties getProperties() {
        return this.properites;
    }

    /**
     * 获取服务器地址
     *
     * @return 服务器地址
     */
    private String getServerHost() {
        return Optional.ofNullable(getProperties())
                .map(NettyClientProperties::getHost)
                .orElse(null);
    }

    /**
     * 获取服务器端口
     *
     * @return 服务器端口
     */
    private Integer getServerPort() {
        return Optional.ofNullable(getProperties())
                .map(NettyClientProperties::getPort)
                .orElse(null);
    }

    /**
     * 获取重连间隔
     *
     * @return 重连间隔
     */
    private Duration getReconnectInterval() {
        return Optional.ofNullable(getProperties())
                .map(NettyClientProperties::getReconnectInterval)
                .orElse(Duration.ZERO);
    }

    /**
     * 启动前置处理
     *
     * @param args 启动参数
     */
    private void preStartHandler(@Nullable final ApplicationArguments args) {
        Optional.of(context.getBeansOfType(PreStartHandler.class))
                .filter(map -> !CollectionUtils.isEmpty(map))
                .ifPresent(map -> map.forEach((name, handler) -> {
                    try {
                        log.info("开始调用 启动前置: {}", name);
                        handler.preHandler(args);
                    } catch (Throwable e) {
                        log.warn("调用启动前置[{}]-exp: {}", name, e.getMessage());
                    }
                }));
    }

    /**
     * 连接成功处理
     *
     * @param channel 连接通道
     */
    private void connectedHandler(@Nonnull final Channel channel) {
        Optional.of(context.getBeansOfType(ConnectedHandler.class))
                .filter(map -> !CollectionUtils.isEmpty(map))
                .ifPresent(map -> map.forEach((name, handler) -> {
                    try {
                        log.info("开始调用连接成功处理器: {}", name);
                        handler.handler(channel);
                    } catch (Throwable e) {
                        log.warn("调用连接成功处理器[{}]-exp: {}", name, e.getMessage());
                    }
                }));
    }

    @Override
    public void run(final ApplicationArguments args) {
        try {
            log.info("Netty-Client 启动...");
            //启动前置处理
            preStartHandler(args);
            //创建客户端启动对象
            this.bootstrap = new Bootstrap();
            //构建Bootstrap配置
            this.buildBootstrap(bootstrap, () -> IS_EPOLL ? EpollSocketChannel.class : NioSocketChannel.class);
            //连接服务器
            this.connectServer();
        } catch (Throwable e) {
            log.warn("Netty-Client 启动失败: {}", e.getMessage());
        }
    }

    @Override
    public void connectServer() {
        try {
            final String host = getServerHost();
            final Integer port = getServerPort();
            log.info("netty start[{}:{}]...", host, port);
            if (Strings.isNullOrEmpty(host) || Objects.isNull(port)) {
                log.error("未配置服务器: [host: {},port: {}]", host, port);
                return;
            }
            Assert.notNull(bootstrap, "'bootstrap'不能为空");
            //启动连接服务端
            final ChannelFuture future = bootstrap.connect(host, port);
            if (Objects.nonNull(future)) {
                future.addListener(f -> {
                    try {
                        //连接服务器结果
                        final boolean ret = f.isSuccess();
                        refConnect.set(ret);
                        if (ret) {
                            refReconnectCount.set(0);
                            log.info("连接服务器({}:{})=>成功", host, port);
                            connectedHandler(future.channel());
                            return;
                        }
                        //判断是否为重连
                        if (refReconnectCount.get() <= 0 && Objects.isNull(refReconnectScheduled.get())) {
                            final Duration interval = getReconnectInterval();
                            log.info("连接服务器({}:{})=>失败,准备开始重连定时任务(interval: {})", host, port, interval);
                            if (Objects.nonNull(interval) && !interval.isZero()) {
                                refReconnectScheduled.set(future.channel()
                                        .eventLoop()
                                        .schedule(this::reconnectHandler, interval.toMillis(), TimeUnit.MILLISECONDS));
                            }
                        }
                    } finally {
                        refReconnectRun.set(false);
                    }
                });
            }
        } catch (Throwable e) {
            refReconnectRun.set(false);
            log.error("connectServer-exp: {}", e.getMessage());
        }
    }

    private void reconnectHandler() {
        //检查连接是否已成功,连接成功关闭重连任务
        if (refConnect.get()) {
            //关闭重连任务
            closeReconnectTask();
            return;
        }
        //检查是否已在重连中
        if (refReconnectRun.get()) {
            log.info("已在重连中...");
            return;
        }
        refReconnectRun.set(true);
        log.info("准备第[{}]次开始重连服务器", refReconnectCount.incrementAndGet());
        connectServer();
    }

    private void closeReconnectTask() {
        Optional.ofNullable(refReconnectScheduled.get())
                .ifPresent(f -> {
                    try {
                        f.cancel(false);
                    } catch (Throwable e) {
                        log.warn("closeReconnectTask-exp: {}", e.getMessage());
                    } finally {
                        refReconnectScheduled.set(null);
                    }
                });
    }

    @Override
    protected void initChannel(@Nonnull final Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        initChannelPipelineHandler(-1, pipeline);
        log.info("已挂载处理器: {}", Joiner.on(",").skipNulls().join(pipeline.names()));
    }

    @Override
    protected void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        super.initChannelPipelineHandler(port, pipeline);
        //1.挂载空闲检查处理器
        Optional.ofNullable(getProperties())
                .map(NettyClientProperties::getHeartbeatInterval)
                .ifPresent(heartbeat -> {
                    pipeline.addLast("idle", new HeartbeatHandler(heartbeat));
                    log.info("Netty-挂载空闲检查处理器: {}", heartbeat);
                });
        //2.挂载业务处理器
        final BaseClientSocketHandler<?> handler = context.getBean(BaseClientSocketHandler.class);
        Assert.notNull(handler, "'BaseClientSocketHandler'子类对象不存在!");
        //检查注解
        ScopeUtils.checkPrototype(handler.getClass());
        //添加到管道
        pipeline.addLast("biz", handler);
    }

    @Override
    public void close() {
        this.closeReconnectTask();
        super.close();
    }

    @Override
    public void destroy() {
        close();
    }
}
