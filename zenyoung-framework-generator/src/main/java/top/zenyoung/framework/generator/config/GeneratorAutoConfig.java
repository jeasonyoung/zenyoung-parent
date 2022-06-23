package top.zenyoung.framework.generator.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.framework.generator.config.GeneratorProperties;

/**
 * 代码生成器配置
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(GeneratorProperties.class)
@EnableConfigurationProperties(GeneratorProperties.class)
@ConditionalOnProperty(prefix = "top.zenyoung.generator", value = "enable", matchIfMissing = true)
public class GeneratorAutoConfig {

}
