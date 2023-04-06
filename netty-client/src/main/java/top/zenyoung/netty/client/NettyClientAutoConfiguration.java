package top.zenyoung.netty.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.client.client.NettyClient;
import top.zenyoung.netty.client.client.impl.NettyClientImpl;
import top.zenyoung.netty.client.config.AsyncEventConfig;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.client.handler.ClientStrategyHandler;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.StrategyFactory;
import top.zenyoung.netty.handler.StrategyFactoryInstance;

import java.util.List;

/**
 * NettyClient-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({AsyncEventConfig.class})
@EnableConfigurationProperties({NettyClientProperties.class})
public class NettyClientAutoConfiguration {

    @Bean("clientStrategyFactory")
    @ConditionalOnMissingBean
    public StrategyFactory strategyFactory(final List<ClientStrategyHandler<? extends Message>> strategies) {
        return StrategyFactoryInstance.instance(strategies);
    }

    @Bean(initMethod = "run", destroyMethod = "close")
    @ConditionalOnMissingBean
    public NettyClient nettyClient(final ObjectProvider<NettyClientProperties> properites, final ObjectProvider<ApplicationContext> contexts) {
        final NettyClientProperties nettyProperites = properites.getIfAvailable();
        final ApplicationContext context = contexts.getIfAvailable();
        log.info("开始启动netty-client: {}", nettyProperites);
        return NettyClientImpl.of(nettyProperites, context);
    }

}
