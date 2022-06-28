package top.zenyoung.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.zenyoung.boot.advice.ExceptionController;
import top.zenyoung.boot.config.*;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.common.util.RandomUtils;

import java.util.Objects;

/**
 * Boot-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@ComponentScan({"top.zenyoung.boot.aop", "top.zenyoung.boot.service"})
@Import({AsyncConfig.class, SwaggerConfig.class, ExceptionController.class})
@EnableConfigurationProperties({RepeatSubmitProperties.class, CaptchaProperties.class, IdSequenceProperties.class})
public class BootAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdSequence.class)
    public IdSequence buildSequence(final ObjectProvider<IdSequenceProperties> provider) {
        final int max = 10;
        final int cpus = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        final IdSequenceProperties properties;
        if (Objects.nonNull(provider) && Objects.nonNull(properties = provider.getIfAvailable())) {
            final int workerId = (Objects.isNull(properties.getWorkerId()) || properties.getWorkerId() < 0) ? (cpus & max) :
                    Math.min((int) SnowFlake.MAX_WORKER_ID, properties.getWorkerId());
            final int dataCenterId = (Objects.isNull(properties.getDataCenterId()) || properties.getDataCenterId() < 0) ? ((cpus * 2) & max) :
                    Math.min((int) SnowFlake.MAX_DATA_CENTER_ID, properties.getDataCenterId());
            final int sequence = (Objects.isNull(properties.getSequence()) || properties.getSequence() < 0) ? RandomUtils.randomInt(0, (int) SnowFlake.MAX_SEQUENCE) :
                    Math.min((int) SnowFlake.MAX_SEQUENCE, properties.getSequence());
            return SnowFlake.getInstance(workerId, dataCenterId, sequence);
        }
        return SnowFlake.getInstance(cpus & max, (cpus * 2) & max);
    }

}
