package top.zenyoung.netty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.config.AsyncEventConfig;
import top.zenyoung.netty.config.NettyProperites;
import top.zenyoung.netty.handler.StrategyHandler;
import top.zenyoung.netty.server.StrategyFactory;
import top.zenyoung.netty.server.NettyServer;
import top.zenyoung.netty.server.impl.StrategyFactoryImpl;
import top.zenyoung.netty.server.impl.NettyServerImpl;

import java.util.List;

/**
 * Netty-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({AsyncEventConfig.class})
@EnableConfigurationProperties({NettyProperites.class})
public class NettyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StrategyFactory strategyFactory(final List<StrategyHandler<? extends Message>> strategies) {
        return new StrategyFactoryImpl(strategies);
    }

    @Bean(initMethod = "run", destroyMethod = "close")
    @ConditionalOnMissingBean
    public NettyServer nettyServer(final ObjectProvider<NettyProperites> properites, final ObjectProvider<ApplicationContext> contexts) {
        final NettyProperites nettyProperites = properites.getIfAvailable();
        final ApplicationContext context = contexts.getIfAvailable();
        log.info("开始启动netty-server: {}", nettyProperites);
        return NettyServerImpl.of(nettyProperites, context);
    }
}
