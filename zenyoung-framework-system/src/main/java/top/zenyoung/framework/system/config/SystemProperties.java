package top.zenyoung.framework.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统模块配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.system")
public class SystemProperties {
    /**
     * 是否启用
     */
    private boolean enable = true;
}
