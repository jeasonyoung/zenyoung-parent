package top.zenyoung.netty.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Netty-配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.netty.server")
public class NettyProperites {
    /**
     * 服务器端口
     */
    private Integer port = 9000;
    /**
     * 保持连接数(默认1024)
     */
    private Integer backlog = 1024;
    /**
     * Netty日志级别
     */
    private String logLevel = "DEBUG";
    /**
     * 心跳间隔(默认5分钟)
     */
    private Duration heartbeatInterval = Duration.ofSeconds(300);
    /**
     * 心跳超时次数
     */
    private Integer heartbeatTimeoutTotal = 3;
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
     * 配置编解码器(编解码器名称,编解码器类或Bean名)
     */
    private Map<String, String> codec;
}
