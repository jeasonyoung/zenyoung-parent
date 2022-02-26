package top.zenyoung.framework.runtime.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 运行时模块-自动配置
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(RuntimeProperties.class)
@ConditionalOnProperty(prefix = "top.zenyoung.runtime", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(RuntimeProperties.class)
public class RuntimeAutoConfiguration {
}
