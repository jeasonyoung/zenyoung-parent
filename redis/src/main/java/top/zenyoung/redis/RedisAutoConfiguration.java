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
import org.springframework.context.annotation.Import;
import top.zenyoung.redis.jetcache.JetCacheRedissonConfig;
import top.zenyoung.redis.lock.LockService;
import top.zenyoung.redis.lock.impl.RedisLockServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
@Import({JetCacheRedissonConfig.class})
@ComponentScan({"top.zenyoung.redis.sync"})
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
    public LockService lockService(final ObjectProvider<RedissonClient> clients) {
        return new RedisLockServiceImpl(clients.getIfAvailable());
    }
}
