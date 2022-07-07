package top.zenyoung.netty.server.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.netty.config.BaseProperties;

import java.util.List;
import java.util.Map;

/**
 * NettyServer-配置
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("top.zenyoung.netty.server")
public class NettyServerProperties extends BaseProperties {
    /**
     * 保持连接数(默认1024)
     */
    private Integer backlog = 1024;
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
    /**
     * 配置编解码器(支持多端口,多协议监听)
     * 端口号: (编解码器名称,编解码器类或Bean名)
     */
    private Map<Integer, Map<String, String>> codec;
}
