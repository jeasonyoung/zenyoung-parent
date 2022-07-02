package top.zenyoung.netty.server.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.netty.prop.BaseProperties;

import java.util.List;

/**
 * Netty-配置
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("top.zenyoung.netty.server")
public class NettyServerProperites extends BaseProperties {
    /**
     * IP地址黑名单
     */
    private List<String> ipAddrBlackList;
    /**
     * IP地址白名单
     */
    private List<String> ipAddrWhiteList;
    /**
     * 请求访问限制
     */
    private RequestLimit limit = new RequestLimit();
}
