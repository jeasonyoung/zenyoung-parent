package top.zenyoung.framework.system.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统模块自动配置
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(SystemProperties.class)
@ConditionalOnProperty(prefix = "top.zenyoung.system", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(SystemProperties.class)
public class SystemAutoConfiguration {

}
