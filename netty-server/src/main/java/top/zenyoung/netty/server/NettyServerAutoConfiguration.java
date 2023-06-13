package top.zenyoung.netty.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.codec.Message;
import top.zenyoung.netty.handler.BaseStrategyHandler;
import top.zenyoung.netty.server.config.AsyncEventConfig;
import top.zenyoung.netty.server.config.NettyServerProperties;
import top.zenyoung.netty.server.server.NettyServer;
import top.zenyoung.netty.strategy.StrategyHandlerFactory;

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
    /**
     * 策略处理器工厂
     *
     * @param handlers 策略处理器集合
     * @return 处理器工厂
     */
    @Bean
    public StrategyHandlerFactory getStrategyHandlerFactory(final List<BaseStrategyHandler<? extends Message>> handlers) {
        return new StrategyHandlerFactory(handlers);
    }
}
