package top.zenyoung.data.r2dbc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;

/**
 * R2dbc-反应式自动配置
 *
 * @author young
 */
@Configuration
@EnableR2dbcAuditing
public class R2dbcAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ReactiveAuditorAware<String> reactiveAuditorAware() {
        return () -> SecurityUtils.getPrincipal()
                .map(UserPrincipal::getAccount);
    }
}
