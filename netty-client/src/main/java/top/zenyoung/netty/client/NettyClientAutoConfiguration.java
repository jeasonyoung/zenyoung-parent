package top.zenyoung.netty.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.client.config.AsyncEventConfig;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.client.server.NettyClient;
import top.zenyoung.netty.client.server.impl.NettyClientImpl;
import top.zenyoung.netty.client.strategy.ClientStrategyHandler;
import top.zenyoung.netty.client.strategy.ClientStrategyHandlerFactory;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;
import top.zenyoung.netty.util.StrategyUtils;

import javax.annotation.Nonnull;
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

    @Bean
    public ClientStrategyHandlerFactory getStrategyHandlerFactory(final List<ClientStrategyHandler> handlers) {
        final StrategyHandlerFactory factory = StrategyUtils.build(handlers);
        return factory::process;
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyClient nettyClient(@Nonnull final NettyClientProperties properties, @Nonnull final ApplicationContext context) {
        return new NettyClientImpl(properties, context);
    }
}
