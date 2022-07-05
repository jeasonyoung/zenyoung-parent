package top.zenyoung.redis.jetcache;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.autoconfigure.ConfigTree;
import com.alicp.jetcache.autoconfigure.ExternalCacheAutoInit;
import com.alicp.jetcache.autoconfigure.JetCacheCondition;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

/**
 * JetCache-redisson配置
 * <p>
 *     https://gitee.com/huangxinyu/jetcache-redisson
 * </p>
 * @author young
 */
@Configuration
@Conditional(JetCacheRedissonConfig.RedissonCondition.class)
public class JetCacheRedissonConfig {

    public static class RedissonCondition extends JetCacheCondition {
        public RedissonCondition() {
            super("redis.redisson");
        }
    }

    @Bean
    public RedissonAutoInit redissonAutoInit() {
        return new RedissonAutoInit();
    }

    public static class RedissonAutoInit extends ExternalCacheAutoInit implements ApplicationContextAware {
        private ApplicationContext context;

        @Override
        protected CacheBuilder initCache(final ConfigTree ct, final String cacheAreaWithPrefix) {
            final RedissonClient client = this.context.getBean(RedissonClient.class);
            final ExternalCacheBuilder<?> builder = RedissonDataCacheBuilder.createBuilder().redissonClient(client);
            parseGeneralConfig(builder, ct);
            return builder;
        }

        @Override
        public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
            this.context = context;
        }
    }

}
