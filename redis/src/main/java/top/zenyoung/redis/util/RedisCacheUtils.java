package top.zenyoung.redis.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
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
        try {
            //保存缓存
            redisTemplate.opsForValue().set(key, val);
        } catch (QueryTimeoutException ex) {
            log.debug("saveCacheValue(key: {},val: {})-exp: {}", key, val, ex.getMessage());
        }
    }

    public static void saveCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key, @Nonnull final String val, @Nonnull final Duration timeout) {
        log.debug("saveCacheValue(key: {},val: {},timeout: {})...", key, val, timeout);
        Assert.hasText(key, "'key'不能为空!");
        Assert.hasText(val, "'val'不能为空!");
        try {
            //保存缓存
            redisTemplate.opsForValue().set(key, val, timeout);
        } catch (QueryTimeoutException ex) {
            log.debug("saveCacheValue(key: {},val: {},timeout: {})-exp: {}", key, val, timeout, ex.getMessage());
        }
    }

    public static String getCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key) {
        log.debug("getCacheValue(key: {})...", key);
        Assert.hasText(key, "'key'不能为空!");
        try {
            //读取缓存
            return redisTemplate.opsForValue().get(key);
        } catch (QueryTimeoutException ex) {
            log.debug("getCacheValue(key: {})-exp: {}", key, ex.getMessage());
        }
        return null;
    }

    public static void clearCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key) {
        log.debug("clearCacheValue(key: {})...", key);
        Assert.hasText(key, "'key'不能为空!");
        try {
            //清空数据
            redisTemplate.delete(key);
        } catch (QueryTimeoutException ex) {
            log.debug("clearCacheValue(key: {})-exp: {}", key, ex.getMessage());
        }
    }

    public static void renewalCacheValue(@Nonnull final StringRedisTemplate redisTemplate, @Nonnull final String key, @Nonnull final Duration renewalTime) {
        log.debug("renewalCacheValue(key: {},renewalTime: {})...", key, renewalTime);
        Assert.hasText(key, "'key'不能为空!");
        try {
            redisTemplate.expire(key, renewalTime);
        } catch (QueryTimeoutException ex) {
            log.debug("renewalCacheValue(key: {},renewalTime: {})-exp: {}", key, renewalTime, ex.getMessage());
        }
    }
}
