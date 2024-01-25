package top.zenyoung.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.advice.ResponseMvcAdviceController;
import top.zenyoung.boot.aop.OperaLogAspect;
import top.zenyoung.boot.aop.OperaLogViewAspect;
import top.zenyoung.boot.aop.PrivacyPolicyAspect;
import top.zenyoung.boot.aop.RequestLogAspect;
import top.zenyoung.boot.config.AsyncConfig;
import top.zenyoung.boot.config.RepeatSubmitProperties;
import top.zenyoung.boot.config.WebMvcConfig;
import top.zenyoung.boot.interceptor.RequestAuthorityInterceptor;
import top.zenyoung.boot.resolver.UserIdMethodArgumentResolver;
import top.zenyoung.common.sequence.Sequence;

import javax.annotation.Nonnull;

/**
 * WebMvc 自动注册
 *
 * @author young
 */
@Configuration
@ComponentScan({"top.zenyoung.boot.controller"})
@Import({AsyncConfig.class, WebMvcConfig.class, ResponseMvcAdviceController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class})
public class BootMvcAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Sequence buildSequence(@Nonnull final IdGeneratorProvider provider) {
        final IdGenerator generator = provider.getShare();
        return generator::generate;
    }

    @Bean
    @ConditionalOnProperty(prefix = "top.zenyoung", name = "request-authorize-check", matchIfMissing = true)
    public RequestAuthorityInterceptor requestAuthorizeInterceptor() {
        return new RequestAuthorityInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestLogAspect requestLogAspect(@Nonnull final ObjectMapper objMapper) {
        return RequestLogAspect.of(objMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public OperaLogAspect operaLogAspect(@Nonnull final ObjectMapper objMapper, @Nonnull final ApplicationContext ctx) {
        return OperaLogAspect.of(objMapper, ctx);
    }

    @Bean
    @ConditionalOnMissingBean
    public OperaLogViewAspect operaLogViewAspect(@Nonnull final ObjectMapper objMapper, @Nonnull final ApplicationContext ctx) {
        return OperaLogViewAspect.of(objMapper, ctx);
    }

    @Bean
    @ConditionalOnMissingBean
    public PrivacyPolicyAspect privacyPolicyAspect() {
        return new PrivacyPolicyAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserIdMethodArgumentResolver userIdMethodArgumentResolver() {
        return new UserIdMethodArgumentResolver();
    }
}
