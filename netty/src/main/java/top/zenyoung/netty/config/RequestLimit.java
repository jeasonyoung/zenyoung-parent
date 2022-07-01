package top.zenyoung.netty.config;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

/**
 * 请求限制配置
 *
 * @author young
 */
@Data
public class RequestLimit implements Serializable {
    /**
     * 计数时长
     */
    private Duration delay = Duration.ofSeconds(60);
    /**
     * 最大访问次数
     */
    private Long max = 300L;
}
