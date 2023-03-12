package top.zenyoung.common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 *
 * @author yangyong
 * @version 1.0
 **/
@Slf4j
public class CacheUtils {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Duration DEF_DURATION = Duration.ofMinutes(30);
    private static final int DEF_MAX_SIZE = 500;

    /**
     * 创建缓存器
     *
     * @param <K> 缓存键
     * @param <V> 缓存值
     * @return 缓存器
     */
    public static <K, V> Cache<K, V> createCache() {
        return createCache(500, 30, TimeUnit.SECONDS);
    }

    /**
     * 创建缓存器
     *
     * @param maxSize  最大缓存量
     * @param duration 缓存期
     * @param timeUnit 时间单位
     * @param <K>      缓存键
     * @param <V>      缓存值
     * @return 缓存器
     */
    public static <K, V> Cache<K, V> createCache(final int maxSize, final int duration, final TimeUnit timeUnit) {
        log.debug("createCache(maxSize: {}, duration: {}, timeUnit: {})...", maxSize, duration, timeUnit);
        if (duration <= 0 || timeUnit == null) {
            return createCache(maxSize, null);
        }
        return CacheBuilder.newBuilder()
                .maximumSize(maxSize > 0 ? maxSize : DEF_MAX_SIZE)
                .expireAfterAccess(duration, timeUnit)
                .build();
    }

    /**
     * 创建缓存器
     *
     * @param maxSize 最大缓存量
     * @param timeout 缓存超时时间
     * @param <K>     缓存键
     * @param <V>     缓存值
     * @return 缓存器
     */
    public static <K, V> Cache<K, V> createCache(final int maxSize, final Duration timeout) {
        return CacheBuilder.newBuilder()
                .maximumSize(maxSize > 0 ? maxSize : DEF_MAX_SIZE)
                .expireAfterAccess(timeout == null ? DEF_DURATION : timeout)
                .build();
    }

    /**
     * 获取缓存值
     *
     * @param cache  缓存器
     * @param key    缓存键
     * @param loader 缓存加载器
     * @param <K>    缓存键
     * @param <V>    缓存值
     * @return 缓存值
     */
    public static <K, V> V getCacheValue(@Nonnull final Cache<K, V> cache, @Nonnull final K key, @Nonnull final Callable<? extends V> loader) {
        return Optional.ofNullable(cache.getIfPresent(key))
                .orElseGet(() -> {
                    final String keyVal = String.valueOf(key);
                    synchronized (LOCKS.computeIfAbsent(keyVal, k -> new Object())) {
                        try {
                            final V data = loader.call();
                            if (Objects.nonNull(data)) {
                                cache.put(key, data);
                            } else {
                                cache.invalidate(key);
                            }
                            return data;
                        } catch (Exception e) {
                            cache.invalidate(key);
                            log.warn("getCacheValue(key: {})-exp: {}", key, e.getMessage());
                            return null;
                        } finally {
                            LOCKS.remove(keyVal);
                        }
                    }
                });
    }
}
