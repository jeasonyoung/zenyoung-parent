package top.zenyoung.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import top.zenyoung.common.util.RedisCacheUtils;
import top.zenyoung.service.CacheService;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Redis缓存-服务接口实现
 *
 * @author yangyong
 * @version 1.0
 **/
@Slf4j
public abstract class AbstractRedisCacheServiceImpl implements CacheService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Duration CACHE_EXPIRE = Duration.ofSeconds(1800);

    private final StringRedisTemplate redisTemplate;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis模板
     */
    protected AbstractRedisCacheServiceImpl(@Nonnull final StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取缓存前缀
     *
     * @return 缓存前缀
     */
    @Nonnull
    protected abstract String getCachePrefix();

    /**
     * 获取缓存默认有效期
     *
     * @return 默认有效期
     */
    @Nonnull
    protected Duration getCacheDefaultExpire() {
        return CACHE_EXPIRE;
    }

    @Override
    public <T extends Serializable> void addCache(@Nonnull final String key, @Nonnull final T data) {
        log.debug("addCache(key: {},data: {})...", key, data);
        Assert.hasText(key, "'key'不能为空!");
        //缓存数据
        addCache(key, data, getCacheDefaultExpire());
    }

    /**
     * 获取Redis缓存键
     *
     * @param key 缓存键
     * @return Redis缓存键
     */
    @Nonnull
    protected String getRedisKey(@Nonnull final String key) {
        final String prefix = getCachePrefix();
        return Strings.isNullOrEmpty(prefix) ? key : prefix + ":" + key;
    }

    /**
     * 数据序列化
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 序列化结果
     */
    protected abstract <T extends Serializable> String serializable(@Nonnull final T data);

    /**
     * 数据反序列化
     *
     * @param json      序列化数据
     * @param dataClass 数据类型Class
     * @param <T>       数据类型
     * @return 数据
     */
    protected abstract <T extends Serializable> T deserializable(@Nonnull final String json, @Nonnull final Class<T> dataClass);

    @Override
    public <T extends Serializable> void addCache(@Nonnull final String key, @Nonnull final T data, @Nonnull final Duration liveTime) {
        log.debug("addCache(key: {},data: {},liveTime: {})...", key, data, liveTime);
        Assert.hasText(key, "'key'不能为空!");
        final String redisKey = getRedisKey(key);
        synchronized (LOCKS.computeIfAbsent(redisKey, k -> new Object())) {
            try {
                //序列化数据
                final String json = serializable(data);
                if (!Strings.isNullOrEmpty(json)) {
                    //缓存数据
                    RedisCacheUtils.saveCacheValue(redisTemplate, redisKey, json, liveTime);
                }
            } finally {
                LOCKS.remove(redisKey);
            }
        }
    }

    @Override
    public <T extends Serializable> T getCache(@Nonnull final String key, @Nonnull final Class<T> dataClass) {
        log.debug("getCache(key: {},dataClass: {})...", key, dataClass);
        Assert.hasText(key, "'key'不能为空!");
        final String redisKey = getRedisKey(key);
        synchronized (LOCKS.computeIfAbsent(redisKey, k -> new Object())) {
            try {
                //读取缓存数据
                final String json = RedisCacheUtils.getCacheValue(redisTemplate, redisKey);
                if (!Strings.isNullOrEmpty(json)) {
                    return deserializable(json, dataClass);
                }
            } finally {
                LOCKS.remove(redisKey);
            }
        }
        return null;
    }

    @Override
    public void clear(@Nonnull final String key) {
        log.debug("clear(key: {})...", key);
        Assert.hasText(key, "'key'不能为空!");
        final String redisKey = getRedisKey(key);
        synchronized (LOCKS.computeIfAbsent(redisKey, k -> new Object())) {
            try {
                //清除缓存
                RedisCacheUtils.clearCacheValue(redisTemplate, redisKey);
            } finally {
                LOCKS.remove(redisKey);
            }
        }
    }

    @Override
    public void renewal(@Nonnull final String key, @Nonnull final Duration renewalTime) {
        log.debug("renewal(key: {},renewalTime: {})...", key, renewalTime);
        Assert.hasText(key, "'key'不能为空!");
        final String redisKey = getRedisKey(key);
        synchronized (LOCKS.computeIfAbsent(redisKey, k -> new Object())) {
            try {
                //缓存续约
                RedisCacheUtils.renewalCacheValue(redisTemplate, redisKey, renewalTime);
            } finally {
                LOCKS.remove(redisKey);
            }
        }
    }

    @Override
    public <T extends Serializable> T cacheHander(@Nonnull final String key, @Nonnull final Class<T> dataClass,
                                                  @Nonnull final Duration expire, @Nonnull final Supplier<T> dataHandler) {
        log.debug("cacheHander(key: {},dataClass: {},expire: {},dataHandler: {})...", key, dataClass, expire, dataHandler);
        Assert.hasText(key, "'key'不能为空!");
        Assert.isTrue(expire.getSeconds() > 0, "'expire'必须有效(至少1s)!");
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                T data = getCache(key, dataClass);
                if (data == null) {
                    data = dataHandler.get();
                    if (data != null) {
                        //缓存数据处理
                        addCache(key, data, expire);
                    }
                }
                return data;
            } finally {
                LOCKS.remove(key);
            }
        }
    }
}
