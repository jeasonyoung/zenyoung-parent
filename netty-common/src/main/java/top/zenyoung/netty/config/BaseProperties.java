package top.zenyoung.netty.config;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

/**
 * 配置基类
 *
 * @author young
 */
@Data
public abstract class BaseProperties implements Serializable {
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
}
