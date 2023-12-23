package top.zenyoung.boot;

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
import top.zenyoung.boot.config.SequenceProperties;
import top.zenyoung.boot.config.WebFluxConfig;
import top.zenyoung.boot.filter.AppHttpFilter;
import top.zenyoung.boot.filter.TraceFilter;
import top.zenyoung.boot.resolver.UserIdMethodArgumentResolver;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.common.util.RandomUtils;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * WebFlux-自动注册
 *
 * @author young
 */
@Configuration
@ComponentScan({"top.zenyoung.boot.controller"})
@Import({AsyncConfig.class, WebFluxConfig.class, ResponseFluxAdviceController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class, SequenceProperties.class})
public class BootWebFluxAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Sequence buildSequence(@Nonnull final SequenceProperties properties) {
        //机器ID
        final long workerId = Optional.ofNullable(properties.getWorkerId())
                .orElseGet(() -> RandomUtils.randomLong(1, SnowFlake.MAX_WORKER_ID - 1));
        //机房ID
        final long dataCenterId = Optional.ofNullable(properties.getDataCenterId())
                .orElseGet(() -> RandomUtils.randomLong(1, SnowFlake.MAX_DATA_CENTER_ID - 1));
        //顺序号
        final long sequence = Optional.ofNullable(properties.getSequence())
                .orElseGet(() -> RandomUtils.randomLong(1, SnowFlake.MAX_SEQUENCE - 1));
        //创建
        return SnowFlake.getInstance(workerId, dataCenterId, sequence);
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
