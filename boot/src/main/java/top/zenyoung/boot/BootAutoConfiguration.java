package top.zenyoung.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.advice.ResponseAdviceController;
import top.zenyoung.boot.aop.OperaLogAspect;
import top.zenyoung.boot.aop.OperaLogViewAspect;
import top.zenyoung.boot.aop.PrivacyPolicyAspect;
import top.zenyoung.boot.aop.RequestLogAspect;
import top.zenyoung.boot.config.*;
import top.zenyoung.boot.interceptor.RequestAuthorizeInterceptor;
import top.zenyoung.boot.resolver.UserIdMethodArgumentResolver;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.boot.service.impl.BeanMappingServiceImpl;
import top.zenyoung.boot.util.IdSequenceUtils;
import top.zenyoung.common.sequence.IdSequence;

import javax.annotation.Nonnull;

/**
 * Boot-自动配置
 *
 * @author young
 */
@Configuration
@ComponentScan({"top.zenyoung.boot.controller"})
@Import({AsyncConfig.class, WebConfig.class, SwaggerConfig.class, Knife4jConfig.class, ResponseAdviceController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class, IdSequenceProperties.class})
public class BootAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdSequence buildSequence(final ObjectProvider<IdSequenceProperties> provider) {
        return IdSequenceUtils.create(provider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanMappingService beanMappingService() {
        return new BeanMappingServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestAuthorizeInterceptor requestAuthorizeInterceptor() {
        return new RequestAuthorizeInterceptor();
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
