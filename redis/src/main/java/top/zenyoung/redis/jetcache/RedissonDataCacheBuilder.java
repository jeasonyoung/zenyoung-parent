package top.zenyoung.redis.jetcache;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import org.redisson.api.RedissonClient;

/**
 * Redisson data cache
 *
 * @author young
 */
public class RedissonDataCacheBuilder<T extends ExternalCacheBuilder<T>> extends ExternalCacheBuilder<T> {

    public static class RedissonDataCacheBuilderImpl extends RedissonDataCacheBuilder<RedissonDataCacheBuilderImpl> {

    }

    public static RedissonDataCacheBuilderImpl createBuilder() {
        return new RedissonDataCacheBuilderImpl();
    }

    @SuppressWarnings({"all"})
    protected RedissonDataCacheBuilder() {
        buildFunc(config -> new RedissonDataCache((RedissonDataCacheConfig) config));
    }

    @Override
    @SuppressWarnings({"all"})
    public RedissonDataCacheConfig getConfig() {
        if (this.config == null) {
            this.config = new RedissonDataCacheConfig();
        }
        return (RedissonDataCacheConfig) this.config;
    }

    public T redissonClient(final RedissonClient client) {
        this.getConfig().setRedissonClient(client);
        return self();
    }
}
