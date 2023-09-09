package top.zenyoung.retrofit.config;

import lombok.Data;

/**
 * 全局超时配置
 *
 * @author young
 */
@Data
public class TimeoutProperty {
    /**
     * 全局连接超时时间
     */
    private int connectTimeoutMs = 10_000;
    /**
     * 全局读取超时时间
     */
    private int readTimeoutMs = 10_000;
    /**
     * 全局写入超时时间
     */
    private int writeTimeoutMs = 10_000;
    /**
     * 全局完整调用超时时间
     */
    private int callTimeoutMs = 0;
}
