package top.zenyoung.boot;

import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.advice.ResponseFluxAdviceController;
import top.zenyoung.boot.aop.RequestAuthorizeAspect;
import top.zenyoung.boot.config.AsyncConfig;
import top.zenyoung.boot.config.RepeatSubmitProperties;
import top.zenyoung.boot.config.WebFluxConfig;
import top.zenyoung.boot.filter.AppHttpFilter;
import top.zenyoung.boot.filter.TraceFilter;
import top.zenyoung.boot.resolver.UserIdMethodArgumentResolver;
import top.zenyoung.common.sequence.Sequence;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * WebFlux-自动注册
 *
 * @author young
 */
@Configuration
@ComponentScan({"top.zenyoung.boot.controller"})
@Import({AsyncConfig.class, WebFluxConfig.class, ResponseFluxAdviceController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class})
public class BootWebFluxAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Sequence buildSequence(@Nonnull final ObjectProvider<IdGeneratorProvider> provider) {
        return () -> {
            final IdGeneratorProvider ip = provider.getIfAvailable();
            if (Objects.nonNull(ip)) {
                final IdGenerator generator = ip.getShare();
                if (Objects.nonNull(generator)) {
                    return generator.generate();
                }
            }
            return -1;
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public AppHttpFilter httpFilter() {
        return new AppHttpFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceFilter traceFilter() {
        return new TraceFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestAuthorizeAspect requestAuthorizeAspect() {
        return new RequestAuthorizeAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserIdMethodArgumentResolver userIdMethodArgumentResolver() {
        return new UserIdMethodArgumentResolver();
    }
}
