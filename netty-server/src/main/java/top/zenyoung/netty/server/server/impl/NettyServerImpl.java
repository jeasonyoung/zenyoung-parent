package top.zenyoung.netty.server.server.impl;

import com.google.common.collect.Maps;
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
import top.zenyoung.netty.util.SocketUtils;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    private Map<Integer, Map<String, String>> portCodec() {
        return Optional.ofNullable(getProperties().getCodec()).orElse(Maps.newHashMap());
    }

    @Override
    protected int getBacklog() {
        return Math.max(this.properites.getBacklog(), 50);
    }

    @Override
    public void run() {
        try {
            log.info("Netty启动...");
            final Map<Integer, Map<String, String>> portCodecMap = portCodec();
            if (CollectionUtils.isEmpty(portCodecMap)) {
                log.error("Netty-未配置服务器监听端口及编解码器!");
                return;
            }
            //启动
            final ServerBootstrap bootstrap = new ServerBootstrap();
            buildBootstrap(bootstrap, () -> IS_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
            final ChannelFuture[] futures = portCodecMap.keySet().stream()
                    .filter(port -> Objects.nonNull(port) && port > 0)
                    .map(port -> {
                        final ChannelFuture future = bootstrap.bind(port).syncUninterruptibly();
                        future.addListener((ChannelFutureListener) f -> log.info("开始监听端口[{}]: {}", port, f.isSuccess() ? "成功" : "失败"));
                        return future;
                    })
                    .toArray(ChannelFuture[]::new);
            syncShutdownHook(futures);
        } catch (Throwable e) {
            log.error("Netty运行失败: {}", e.getMessage());
        }
    }

    @Override
    protected void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        //1.挂载IP地址过滤器
        pipeline.addLast("ipFilter", new IpAddrFilter(properites));
        //2.挂载请求限制过滤器
        if (Objects.nonNull(properites.getLimit())) {
            pipeline.addLast("limitFilter", new RequestLimitFilter(properites));
        }
        //3.挂载空闲检查处理器
        final Duration heartbeat = this.properites.getHeartbeatInterval();
        if (Objects.nonNull(heartbeat)) {
            pipeline.addLast("idle", new HeartbeatHandler(heartbeat));
        }
        //4.挂载编解码器
        final Map<String, String> codecMap = portCodec().getOrDefault(port, null);
        final Map<String, ChannelHandler> codecHandlerMap = CodecUtils.getCodecMap(context, codecMap, true);
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
    }
}
