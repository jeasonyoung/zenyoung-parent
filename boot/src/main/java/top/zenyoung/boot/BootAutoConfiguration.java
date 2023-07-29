package top.zenyoung.boot;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.advice.ResponseAdviceController;
import top.zenyoung.boot.config.*;
import top.zenyoung.boot.service.BeanMappingService;
import top.zenyoung.boot.service.impl.BeanMappingServiceImpl;
import top.zenyoung.boot.util.IdSequenceUtils;
import top.zenyoung.common.sequence.IdSequence;

/**
 * Boot-自动配置
 *
 * @author young
 */
@Configuration
@ComponentScan({
        "top.zenyoung.boot.aop",
        "top.zenyoung.boot.resolver",
        "top.zenyoung.boot.controller"
})
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
}
