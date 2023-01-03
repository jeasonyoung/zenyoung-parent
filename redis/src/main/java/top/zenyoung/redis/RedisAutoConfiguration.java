package top.zenyoung.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.zenyoung.boot.service.CaptchaStorageService;
import top.zenyoung.redis.lock.LockService;
import top.zenyoung.redis.lock.impl.RedisLockServiceImpl;
import top.zenyoung.redis.service.QueueService;
import top.zenyoung.redis.service.RedisEnhancedService;
import top.zenyoung.redis.service.impl.RedisCaptchaStorageServiceImpl;
import top.zenyoung.redis.service.impl.RedisEnhancedServiceImpl;
import top.zenyoung.redis.service.impl.RedisQueueServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@ComponentScan({"top.zenyoung.redis.aop"})
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheManager redisCacheManager(final ObjectProvider<RedissonClient> clients) {
        final RedissonClient client = clients.getIfAvailable();
        final Map<String, ? extends CacheConfig> config = new HashMap<String, CacheConfig>(1) {
            {
                put("zy-spring-cache", new CacheConfig(30 * 60 * 1000, 12 * 60 * 1000));
            }
        };
        return new RedissonSpringCacheManager(client, config);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisEnhancedService enhancedService() {
        return new RedisEnhancedServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueService queueService(final ObjectProvider<RedissonClient> clients) {
        return new RedisQueueServiceImpl(clients.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public LockService lockService(final ObjectProvider<RedissonClient> clients) {
        return new RedisLockServiceImpl(clients.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaStorageService captchaStorageService(final ObjectProvider<StringRedisTemplate> redisTemplate) {
        return RedisCaptchaStorageServiceImpl.of(redisTemplate.getIfAvailable());
    }
}
