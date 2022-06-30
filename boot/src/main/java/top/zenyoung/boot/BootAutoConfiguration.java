package top.zenyoung.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.zenyoung.boot.advice.ExceptionController;
import top.zenyoung.boot.config.*;
import top.zenyoung.boot.service.*;
import top.zenyoung.boot.service.impl.*;
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
    public RedisEnhancedService enhancedService() {
        return new RedisEnhancedServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "top.zenyoung.captcha.enable", havingValue = "true")
    public CaptchaService captchaService(final ObjectProvider<CaptchaProperties> properties,
                                         final ObjectProvider<StringRedisTemplate> redisTemplates,
                                         final ObjectProvider<ApplicationContext> contexts) {
        final CaptchaProperties cp = properties.getIfAvailable();
        final StringRedisTemplate srt = redisTemplates.getIfAvailable();
        final ApplicationContext ctx = contexts.getIfAvailable();
        return new CaptchaServiceImpl(cp, srt, ctx);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "top.zenyoung.queue.enable", havingValue = "true")
    public QueueService redisQueueService(final ObjectProvider<ObjectMapper> objectMappers,
                                          final ObjectProvider<StringRedisTemplate> redisTemplates) {
        final ObjectMapper objectMapper = objectMappers.getIfAvailable();
        final StringRedisTemplate redisTemplate = redisTemplates.getIfAvailable();
        return new RedisQueueServiceImpl(objectMapper, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "top.zenyoung.lock.enable", havingValue = "true")
    public LockService redisLockService(final ObjectProvider<RedissonClient> redissonClients) {
        final RedissonClient client = redissonClients.getIfAvailable();
        return new RedisLockServiceImpl(client);
    }
}
