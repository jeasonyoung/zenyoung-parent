package top.zenyoung.netty.server.server.impl;

import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.BaseNettyImpl;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.handler.BaseServerSocketHandler;
import top.zenyoung.netty.server.handler.IpAddrFilter;
import top.zenyoung.netty.server.server.NettyServer;
import top.zenyoung.netty.util.CodecUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * NettyServer服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NettyServerImpl extends BaseNettyImpl<NettyServerProperties> implements NettyServer, DisposableBean {
    private final NettyServerProperties properites;
    private final ApplicationContext context;

    @Override
    protected NettyServerProperties getProperties() {
        return this.properites;
    }

    private Map<Integer, Map<String, String>> getPortCodecs() {
        return Optional.ofNullable(getProperties().getCodec()).orElse(Maps.newHashMap());
    }

    @Override
    protected int getBacklog() {
        return Math.max(this.properites.getBacklog(), 50);
    }

    @Override
    public void run(final ApplicationArguments args) {
        log.info("Netty-Server 启动...");
        final List<Integer> ports = getPortCodecs().keySet().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ports)) {
            log.error("Netty-Server-未配置端口及编解码器!");
            return;
        }
        //启动
        final ServerBootstrap bootstrap = new ServerBootstrap();
        buildBootstrap(bootstrap, () -> IS_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
        ports.forEach(port -> {
            //启动端口监听
            bootstrap.bind(port).addListener(future -> {
                final boolean ret = future.isSuccess();
                log.info("开始监听端口[{}]: {}", port, ret ? "成功" : "失败");
            });
        });
    }

    @Override
    protected void initChannelPipelineHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        //1.挂载IP地址过滤器
        pipeline.addLast("ipFilter", new IpAddrFilter(properites));
        //2.挂载编解码器
        final Map<String, String> codecMap = getPortCodecs().getOrDefault(port, Maps.newHashMap());
        Assert.notEmpty(codecMap, port + ",未配置编解码器,请检查配置");
        final Map<String, ChannelHandler> codecHandlerMap = Optional.ofNullable(context)
                .map(ctx -> CodecUtils.getCodecMap(ctx, codecMap, true))
                .orElse(null);
        Assert.notNull(codecHandlerMap, port + ",编解码器配置无效!");
        codecHandlerMap.forEach(pipeline::addLast);
        log.info("端口[{}]挂载编解码器: {}", port, codecHandlerMap.keySet());
        //3.挂载业务处理器
        addBizSocketHandler(port, pipeline);
    }

    /**
     * 添加业务处理器
     *
     * @param port     处理监听端口
     * @param pipeline 通道管道
     */
    protected void addBizSocketHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        final Map<String, ?> handlerMap = context.getBeansOfType(BaseServerSocketHandler.class);
        Assert.notEmpty(handlerMap, "未配置'BaseServerSocketHandler'处理器");
        final AtomicBoolean ref = new AtomicBoolean(false);
        handlerMap.forEach((name, handler) -> {
            final BaseServerSocketHandler<?> socketHandler = (BaseServerSocketHandler<?>) handler;
            socketHandler.ensureHasScope();
            if (socketHandler.supportedPort(port)) {
                pipeline.addLast("biz_" + name, socketHandler);
                ref.set(true);
            }
        });
        if (!ref.get()) {
            log.warn("[port: {}] 未配置业务处理器", port);
        }
    }

    @Override
    public void destroy() {
        super.close();
    }
}
