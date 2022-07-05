package top.zenyoung.redis.jetcache;

import com.alicp.jetcache.external.ExternalCacheConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.redisson.api.RedissonClient;

/**
 * RedissonCacheConfig
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RedissonDataCacheConfig<K, V> extends ExternalCacheConfig<K, V> {
    private RedissonClient redissonClient;
}
