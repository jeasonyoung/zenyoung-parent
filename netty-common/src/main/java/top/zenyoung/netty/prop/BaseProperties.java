package top.zenyoung.netty.prop;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

/**
 * 配置基类
 *
 * @author young
 */
@Data
public abstract class BaseProperties implements Serializable {
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
     * 配置编解码器(编解码器名称,编解码器类或Bean名)
     */
    private Map<String, String> codec;
}
