package top.zenyoung.netty.server.server.impl;

import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.zenyoung.netty.BaseNettyImpl;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.handler.BaseServerSocketHandler;
import top.zenyoung.netty.server.server.NettyServer;
import top.zenyoung.netty.util.CodecUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
                .toList();
        if (CollectionUtils.isEmpty(ports)) {
            log.error("Netty-Server-未配置端口及编解码器!");
            return;
        }
        //启动
        final ServerBootstrap bootstrap = new ServerBootstrap();
        buildBootstrap(bootstrap);
        for (final Integer port : ports) {
            if (Objects.isNull(port) || port <= 0) {
                continue;
            }
            //启动端口监听
            bootstrap.bind(port).addListener(future -> {
                final boolean ret = future.isSuccess();
                log.info("开始监听端口[{}]: {}", port, ret ? "成功" : "失败");
            });
        }
    }

    @Override
    protected void initChannelCodecHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        final Map<String, String> codecMap = getPortCodecs().getOrDefault(port, null);
        if (!CollectionUtils.isEmpty(codecMap)) {
            final Map<String, ChannelHandler> codecHandlerMap = Optional.ofNullable(context)
                    .map(ctx -> CodecUtils.getCodecMap(ctx, codecMap, true))
                    .orElse(null);
            if (!CollectionUtils.isEmpty(codecHandlerMap)) {
                codecHandlerMap.forEach(pipeline::addLast);
            }
        }
    }

    @Override
    protected void initBizHandlers(final int port, @Nonnull final ChannelPipeline pipeline) {
        final var handlerMap = context.getBeansOfType(BaseServerSocketHandler.class);
        if (!CollectionUtils.isEmpty(handlerMap)) {
            handlerMap.forEach((name, handler) -> {
                handler.ensureHasScope();
                if (handler.supportedPort(port)) {
                    pipeline.addLast("biz_" + name, handler);
                }
            });
        }
    }

    @Override
    public void destroy() {
        super.close();
    }
}
