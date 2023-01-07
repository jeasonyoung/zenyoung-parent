package top.zenyoung.netty.config;

import com.google.common.base.Strings;
import io.netty.handler.logging.LogLevel;
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

    /**
     * 获取Netty日志级别
     *
     * @return Netty日志级别
     */
    public LogLevel getNettyLogLevel() {
        final String logLevel = this.getLogLevel();
        if (!Strings.isNullOrEmpty(logLevel)) {
            for (final LogLevel level : LogLevel.values()) {
                if (logLevel.equalsIgnoreCase(level.name())) {
                    return level;
                }
            }
        }
        return LogLevel.INFO;
    }
}
