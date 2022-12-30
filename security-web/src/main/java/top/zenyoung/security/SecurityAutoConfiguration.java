package top.zenyoung.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.security.config.SecurityConfig;

/**
 * Web安全自动配置
 *
 * @author young
 */
@Configuration
@Import({SecurityConfig.class})
public class SecurityAutoConfiguration {

}