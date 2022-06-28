package top.zenyoung.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.time.Duration;

/**
 * 防止重复提交配置
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.repeat")
public class RepeatSubmitProperties implements Serializable {
    /**
     * 是否启用
     */
    private boolean enabled = true;
    /**
     * 间隔时间
     */
    private Duration interval = Duration.ofMinutes(5);
}
