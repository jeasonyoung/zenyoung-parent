package top.zenyoung.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security配置
 *
 * @author young
 */
@Data
@ConfigurationProperties("top.zenyoung.security")
public class SecurityProperties {

}
