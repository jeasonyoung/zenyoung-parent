package top.zenyoung.redis.jetcache;

import com.alicp.jetcache.*;
import com.alicp.jetcache.external.AbstractExternalCache;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redisson Data Cache
 *
 * @param <K>
 * @param <V>
 * @author young
 */
@Slf4j
public class RedissonDataCache<K, V> extends AbstractExternalCache<K, V> {
    private final RedissonClient client;
    private final RedissonDataCacheConfig<K, V> config;

    public RedissonDataCache(final RedissonDataCacheConfig<K, V> config) {
        super(config);
        this.config = config;
        this.client = config.getRedissonClient();
    }

    protected String getCacheKey(final K key) {
        final byte[] newKey = buildKey(key);
        return new String(newKey, StandardCharsets.UTF_8);
    }

    @Override
    public CacheConfig<K, V> config() {
        return this.config;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        throw new UnsupportedOperationException("RedissonDataCache does not support unwrap");
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected CacheGetResult<V> do_GET(final K key) {
        try {
            final RBucket<CacheValueHolder<V>> rb = this.client.getBucket(getCacheKey(key));
            final CacheValueHolder<V> holder = rb.get();
            if (Objects.nonNull(holder)) {
                if (System.currentTimeMillis() >= holder.getExpireTime()) {
                    return CacheGetResult.EXPIRED_WITHOUT_MSG;
                }
                return new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder);
            }
            return CacheGetResult.NOT_EXISTS_WITHOUT_MSG;
        } catch (Throwable e) {
            log.warn("do_GET(key: {})-exp: {}", key, e.getMessage());
            return new CacheGetResult<>(e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected MultiGetResult<K, V> do_GET_ALL(final Set<? extends K> keys) {
        try {
            final Map<K, CacheGetResult<V>> retMap = CollectionUtils.isEmpty(keys) ? Maps.newHashMap() :
                    keys.stream()
                            .map(key -> {
                                final RBucket<CacheValueHolder<V>> rb = this.client.getBucket(getCacheKey(key));
                                final CacheValueHolder<V> holder = rb.get();
                                if (Objects.nonNull(holder)) {
                                    if (System.currentTimeMillis() >= holder.getExpireTime()) {
                                        return Pair.of(key, CacheGetResult.EXPIRED_WITHOUT_MSG);
                                    }
                                    return Pair.of(key, new CacheGetResult<>(CacheResultCode.SUCCESS, null, holder));
                                }
                                return Pair.of(key, CacheGetResult.NOT_EXISTS_WITHOUT_MSG);
                            })
                            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (n, o) -> n));
            return new MultiGetResult<>(CacheResultCode.SUCCESS, null, retMap);
        } catch (Throwable e) {
            log.warn("do_GET_ALL(keys: {})-exp: {}", keys, e.getMessage());
            return new MultiGetResult<>(e);
        }
    }

    @Override
    protected CacheResult do_PUT(final K key, final V value, final long expireAfterWrite, final TimeUnit timeUnit) {
        final RBucket<CacheValueHolder<V>> rb = this.client.getBucket(getCacheKey(key));
        final CacheValueHolder<V> holder = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
        rb.set(holder, expireAfterWrite, TimeUnit.MILLISECONDS);
        return CacheGetResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_PUT_ALL(final Map<? extends K, ? extends V> map, final long expireAfterWrite, final TimeUnit timeUnit) {
        if (!CollectionUtils.isEmpty(map)) {
            final RBatch batch = this.client.createBatch();
            map.forEach((k, v) -> {
                final CacheValueHolder<V> holder = new CacheValueHolder<>(v, timeUnit.toMillis(expireAfterWrite));
                batch.getBucket(getCacheKey(k)).setAsync(holder, expireAfterWrite, TimeUnit.MILLISECONDS);
            });
            batch.execute();
        }
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE(final K key) {
        final RBucket<CacheValueHolder<V>> rb = this.client.getBucket(getCacheKey(key));
        rb.delete();
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_REMOVE_ALL(final Set<? extends K> keys) {
        if (!CollectionUtils.isEmpty(keys)) {
            final RBatch batch = this.client.createBatch();
            keys.forEach(key -> batch.getBucket(getCacheKey(key)).deleteAsync());
            batch.execute();
        }
        return CacheResult.SUCCESS_WITHOUT_MSG;
    }

    @Override
    protected CacheResult do_PUT_IF_ABSENT(final K key, final V value, final long expireAfterWrite, final TimeUnit timeUnit) {
        final CacheValueHolder<V> holder = new CacheValueHolder<>(value, timeUnit.toMillis(expireAfterWrite));
        final RBucket<CacheValueHolder<V>> rb = this.client.getBucket(getCacheKey(key));
        final boolean success = rb.trySet(holder, expireAfterWrite, TimeUnit.MILLISECONDS);
        return success ? CacheResult.SUCCESS_WITHOUT_MSG : CacheResult.EXISTS_WITHOUT_MSG;
    }
}