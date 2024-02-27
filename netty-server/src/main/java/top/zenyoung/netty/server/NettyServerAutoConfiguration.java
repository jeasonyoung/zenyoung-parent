package top.zenyoung.netty.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.server.config.AsyncEventConfig;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.server.NettyServer;
import top.zenyoung.netty.server.server.impl.NettyServerImpl;
import top.zenyoung.netty.server.strategy.ServerStrategyHandler;
import top.zenyoung.netty.server.strategy.ServerStrategyHandlerFactory;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;
import top.zenyoung.netty.util.StrategyUtils;

import javax.annotation.Nonnull;
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

    @Bean
    @ConditionalOnMissingBean
    public <M extends Message> ServerStrategyHandlerFactory getStrategyHandlerFactory(final List<ServerStrategyHandler<M>> handlers) {
        final StrategyHandlerFactory factory = StrategyUtils.build(handlers);
        return factory::process;
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyServer nettyServer(@Nonnull final NettyServerProperties properties) {
        return new NettyServerImpl(properties);
    }
}
