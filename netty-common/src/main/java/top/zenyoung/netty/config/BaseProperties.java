package top.zenyoung.netty.config;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.handler.logging.LogLevel;
import lombok.Data;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置基类
 *
 * @author young
 */
@Data
public abstract class BaseProperties implements Serializable {
    private final Map<String, LogLevel> logLevelMap = Maps.newHashMap();
    /**
     * Netty日志级别
     */
    private String logLevel = "DEBUG";
    /**
     * 心跳间隔(默认30秒)
     */
    private Duration heartbeatInterval = Duration.ofSeconds(30);
    /**
     * 心跳超时次数
     */
    private Integer heartbeatTimeoutTotal = 3;
    /**
     * IP地址黑名单
     */
    private List<String> ipAddrBlackList = Lists.newArrayList();
    /**
     * IP地址白名单
     */
    private List<String> ipAddrWhiteList = Lists.newArrayList();

    /**
     * 获取Netty日志级别
     *
     * @return Netty日志级别
     */
    public LogLevel getNettyLogLevel() {
        return Optional.ofNullable(getLogLevel())
                .filter(level -> !Strings.isNullOrEmpty(level))
                .map(level -> logLevelMap.computeIfAbsent(level, k -> {
                    for (final LogLevel ll : LogLevel.values()) {
                        if (logLevel.equalsIgnoreCase(ll.name())) {
                            return ll;
                        }
                    }
                    return null;
                }))
                .orElse(LogLevel.INFO);
    }
}
