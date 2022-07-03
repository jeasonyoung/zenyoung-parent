package top.zenyoung.netty.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.StrategyFactory;
import top.zenyoung.netty.handler.StrategyFactoryInstance;
import top.zenyoung.netty.server.config.AsyncEventConfig;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.handler.StrategyHandler;
import top.zenyoung.netty.server.server.NettyServer;
import top.zenyoung.netty.server.server.impl.NettyServerImpl;

import java.util.List;

/**
 * NettyServer-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({AsyncEventConfig.class})
@EnableConfigurationProperties({NettyServerProperties.class})
public class NettyServerAutoConfiguration {

    @Bean("serverStrategyFactory")
    @ConditionalOnMissingBean
    public StrategyFactory strategyFactory(final List<StrategyHandler<? extends Message>> strategies) {
        return StrategyFactoryInstance.instance(strategies);
    }

    @Bean(initMethod = "run", destroyMethod = "close")
    @ConditionalOnMissingBean
    public NettyServer nettyServer(final ObjectProvider<NettyServerProperties> properites, final ObjectProvider<ApplicationContext> contexts) {
        final NettyServerProperties nettyProperites = properites.getIfAvailable();
        final ApplicationContext context = contexts.getIfAvailable();
        log.info("开始启动netty-server: {}", nettyProperites);
        return NettyServerImpl.of(nettyProperites, context);
    }
}
