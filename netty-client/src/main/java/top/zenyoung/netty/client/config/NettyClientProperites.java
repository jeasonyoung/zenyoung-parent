package top.zenyoung.netty.client.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.netty.prop.BaseProperties;

/**
 * NettyClient-配置
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("top.zenyoung.netty.server")
public class NettyClientProperites extends BaseProperties {

}
