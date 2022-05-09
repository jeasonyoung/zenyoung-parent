package top.zenyoung.framework.runtime.config;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;

/**
 * 防止重复提交配置
 * @author young
 */
@Data
public class RepeatSubmitConfig implements Serializable {
    /**
     * 是否启用
     */
    private boolean enabled = true;
    /**
     * 间隔时间
     */
    private Duration interval = Duration.ofMinutes(5);
}
