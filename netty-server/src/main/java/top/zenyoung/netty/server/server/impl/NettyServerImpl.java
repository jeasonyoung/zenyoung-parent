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

    /**
     * 获取Bean对象
     *
     * @param cls Bean类型
     * @param <T> Bean对象类型
     * @return Bean对象
     */
    protected final <T> T getBean(@Nonnull final Class<T> cls) {
        return context.getBean(cls);
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
        //2.挂载请求限制过滤器
        Optional.ofNullable(getProperties())
                .map(NettyServerProperties::getLimit)
                .ifPresent(limit -> pipeline.addLast("limitFilter", new RequestLimitFilter(properites)));
        //3.挂载空闲检查处理器
        Optional.ofNullable(getProperties())
                .map(NettyServerProperties::getHeartbeatInterval)
                .ifPresent(heartbeat -> pipeline.addLast("idle", new HeartbeatHandler(heartbeat)));
        //4.挂载编解码器
        final Map<String, String> codecMap = getPortCodecs().getOrDefault(port, Maps.newHashMap());
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
        addBizSocketHandler(port, pipeline);
    }

    /**
     * 添加业务处理器
     *
     * @param port     处理监听端口
     * @param pipeline 通道管道
     */
    protected void addBizSocketHandler(final int port, @Nonnull final ChannelPipeline pipeline) {
        pipeline.addLast("biz", getBean(BaseServerSocketHandler.class));
    }

    @Override
    public void destroy() {
        super.close();
    }
}
