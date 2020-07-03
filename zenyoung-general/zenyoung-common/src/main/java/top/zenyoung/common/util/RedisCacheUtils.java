package top.zenyoung.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Redis-缓存-工具类
 *
 * @author yangyong
 * @version 1.0
 **/
@Slf4j
public class RedisCacheUtils {

    public static void saveCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key, @Nonnull final String val) {
        log.debug("saveCacheValue(key: {},val: {})...", key, val);
        Assert.hasText(key, "'key'不能为空!");
        Assert.hasText(val, "'val'不能为空!");
        //缓存数据
        saveCacheValue(redisTemplate, key, val, Duration.ofHours(2));
    }

    public static void saveCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key, @Nonnull final String val, @Nonnull final Duration timeout) {
        log.debug("saveCacheValue(key: {},val: {},timeout: {})...", key, val, timeout);
        Assert.hasText(key, "'key'不能为空!");
        Assert.hasText(val, "'val'不能为空!");
        //保存缓存
        redisTemplate.opsForValue().set(key, val, timeout);
    }

    public static String getCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key) {
        log.debug("getCacheValue(key: {})...", key);
        Assert.hasText(key, "'key'不能为空!");
        //读取缓存
        return redisTemplate.opsForValue().get(key);
    }

    public static void clearCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key) {
        log.debug("clearCacheValue(key: {})...", key);
        Assert.hasText(key, "'key'不能为空!");
        //清空数据
        redisTemplate.delete(key);
    }
}
