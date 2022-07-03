package top.zenyoung.netty.client.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.netty.prop.BaseProperties;

import java.time.Duration;

/**
 * NettyClient-配置
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("top.zenyoung.netty.server")
public class NettyClientProperties extends BaseProperties {
    /**
     * Netty服务器IP地址
     */
    private String serverIp;
    /**
     * 重连服务器间隔
     */
    private Duration reconnectInterval = Duration.ofSeconds(10);
}
