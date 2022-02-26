package top.zenyoung.framework.runtime.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 运行时模块配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.runtime")
public class RuntimeProperties {
    
}
