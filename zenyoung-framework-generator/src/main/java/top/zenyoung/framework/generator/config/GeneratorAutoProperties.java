package top.zenyoung.framework.generator.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 代码生成器配置
 *
 * @author young
 */
@Data
@Configuration
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(value = "zenyoung.generator")
@ConditionalOnProperty(prefix = "zenyoung.generator", name = "enable", havingValue = "true", matchIfMissing = true)
public class GeneratorAutoProperties extends GeneratorProperties {
    /**
     * 启用配置
     */
    private boolean enable = true;
    /**
     * 是否独立部署
     */
    private boolean alone = false;
    /**
     * 是否启动跨域
     */
    private boolean cors = true;
}
