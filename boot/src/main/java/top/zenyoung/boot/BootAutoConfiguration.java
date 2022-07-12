package top.zenyoung.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.advice.ExceptionController;
import top.zenyoung.boot.config.*;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.boot.service.CaptchaService;
import top.zenyoung.boot.service.impl.BeanMappingServiceImpl;
import top.zenyoung.boot.service.impl.CaptchaServiceImpl;
import top.zenyoung.boot.util.IdSequenceUtils;
import top.zenyoung.common.sequence.IdSequence;

/**
 * Boot-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@ComponentScan({"top.zenyoung.boot.aop"})
@Import({AsyncConfig.class, SwaggerConfig.class, ExceptionController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class, CaptchaProperties.class, IdSequenceProperties.class})
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
    @ConditionalOnProperty(prefix = "top.zenyoung.captcha", name = "enable", havingValue = "true")
    public CaptchaService captchaService(final ObjectProvider<CaptchaProperties> properties,
                                         final ObjectProvider<ApplicationContext> contexts) {
        final CaptchaProperties cp = properties.getIfAvailable();
        final ApplicationContext ctx = contexts.getIfAvailable();
        final CaptchaServiceImpl impl = new CaptchaServiceImpl(cp, ctx);
        //初始化
        impl.init();
        //返回
        return impl;
    }
}
