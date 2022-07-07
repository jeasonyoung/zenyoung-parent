package top.zenyoung.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.security.config.SecurityConfig;
import top.zenyoung.security.config.SecurityProperties;
import top.zenyoung.security.token.TokenLimitService;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.token.TokenVerifyService;
import top.zenyoung.security.token.impl.TokenLimitServiceImpl;
import top.zenyoung.security.token.impl.TokenServiceImpl;
import top.zenyoung.security.token.impl.TokenVerifyServiceImpl;

/**
 * Spring安全模块-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({SecurityConfig.class})
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenService tokenService(final ApplicationContext context) {
        return new TokenServiceImpl(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenVerifyService verifyService(final ApplicationContext context) {
        return new TokenVerifyServiceImpl(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenLimitService limitService(final ApplicationContext context) {
        return new TokenLimitServiceImpl(context);
    }
}
