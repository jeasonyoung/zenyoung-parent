package top.zenyoung.netty.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.StrategyFactory;
import top.zenyoung.netty.handler.StrategyFactoryInstance;
import top.zenyoung.netty.server.config.AsyncEventConfig;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.handler.StrategyHandler;
import top.zenyoung.netty.server.server.NettyServer;

import java.util.List;

/**
 * NettyServer-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({AsyncEventConfig.class})
@ComponentScan(basePackageClasses = {NettyServer.class})
@EnableConfigurationProperties({NettyServerProperties.class})
public class NettyServerAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean("serverStrategyFactory")
    public StrategyFactory strategyFactory(final List<StrategyHandler<? extends Message>> strategies) {
        return StrategyFactoryInstance.instance(strategies);
    }
}
