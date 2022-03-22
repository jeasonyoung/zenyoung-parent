package top.zenyoung.framework.runtime.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 运行时模块配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung")
public class RuntimeProperties {
    /**
     * 防止重复提交
     */
    private RepeatSubmitConfig repeatSubmit = new RepeatSubmitConfig();

    /**
     * 防止重复提交配置
     */
    @Data
    public static final class RepeatSubmitConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        /**
         * 间隔时间
         */
        private Duration interval = Duration.ofMinutes(5);
    }
}
