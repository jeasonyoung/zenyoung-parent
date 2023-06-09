package top.zenyoung.netty.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.netty.client.config.AsyncEventConfig;
import top.zenyoung.netty.client.config.NettyClientProperties;
import top.zenyoung.netty.client.server.NettyClient;

/**
 * NettyClient-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({AsyncEventConfig.class})
@ComponentScan(basePackageClasses = {NettyClient.class})
@EnableConfigurationProperties({NettyClientProperties.class})
public class NettyClientAutoConfiguration {

}
