package top.zenyoung.service.impl;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import top.zenyoung.service.SyncLockService;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * redis同步锁-服务接口实现
 *
 * @author yangyong
 * @version 1.0
 **/
@Slf4j
public abstract class AbstractRedisSyncLockServiceImpl implements SyncLockService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<String, Integer> LOCK_WAIT_COUNT = Maps.newConcurrentMap();

    private static final Duration DEF_LOCK_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEF_LOCK_WAIT_TIME = Duration.ofMillis(200);
    private static final int DEF_LOCK_WAIT_MAX = 10;

    private final StringRedisTemplate redisTemplate;

    /**
     * 构造函数
     *
     * @param redisTemplate redis模板
     */
    protected AbstractRedisSyncLockServiceImpl(@Nonnull final StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取同步锁键
     *
     * @param key 键名
     * @return 同步锁键
     */
    protected abstract String getSyncLockKey(@Nonnull final String key);

    /**
     * 创建同步锁
     *
     * @param key     同步锁键
     * @param timeout 创建超时
     * @return 创建结果
     */
    protected boolean tryAcquire(@Nonnull final String key, @Nonnull final Duration timeout) {
        log.debug("tryAcquire(key: {},timeout: {})...", key, timeout);
        Assert.hasText(key, "'key'不能为空!");
        try {
            //创建同步锁
            final Boolean ret = redisTemplate.opsForValue().setIfAbsent(key, System.currentTimeMillis() + "", timeout);
            return ret != null && ret;
        } catch (Throwable ex) {
            log.warn("tryAcquire(key: {},timeout: {})-exp: {}", key, timeout, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * 释放同步锁
     *
     * @param key 同步锁键
     */
    protected void releaseLock(@Nonnull final String key) {
        log.debug("releaseLock(key: {})...", key);
        Assert.hasText(key, "'key'不能为空!");
        try {
            //检查同步锁是否存在
            final Boolean exists = redisTemplate.hasKey(key);
            if (exists != null && exists) {
                //删除同步锁键
                redisTemplate.delete(key);
            }
        } catch (Throwable ex) {
            log.warn("releaseLock(key: {})-exp: {}", key, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Nonnull
    protected Duration getLockTimeout() {
        return DEF_LOCK_TIMEOUT;
    }

    @Nonnull
    protected Integer getWaitMax() {
        return DEF_LOCK_WAIT_MAX;
    }

    @Nonnull
    protected Duration getWaitTime() {
        return DEF_LOCK_WAIT_TIME;
    }

    @Override
    public <T> T syncLock(@Nonnull final String key, @Nonnull final Supplier<T> handler) {
        log.debug("syncLock(key: {},handler: {})...", key, handler);
        Assert.hasText(key, "'key'不能为空!");
        final String syncKey = getSyncLockKey(key);
        Assert.hasText(syncKey, "'syncKey'不能为空!");
        synchronized (LOCKS.computeIfAbsent(syncKey, k -> new Object())) {
            final AtomicBoolean has = new AtomicBoolean(false);
            try {
                //获取锁处理
                if (!tryAcquire(syncKey, getLockTimeout())) {
                    //获取等待次数
                    final int count = LOCK_WAIT_COUNT.getOrDefault(syncKey, 0);
                    if (count >= getWaitMax()) {
                        throw new RuntimeException("等待锁超时[count: " + count + "]!");
                    }
                    try {
                        //等待次数累计
                        LOCK_WAIT_COUNT.put(syncKey, count + 1);
                        //线程等待
                        Thread.sleep((long) (getWaitTime().toMillis() * (1 + count * Math.random())));
                    } catch (Throwable ex) {
                        log.warn("syncLock(key: {},handler: {})-exp: {}", key, handler, ex.getMessage());
                    }
                    //递归调用
                    return syncLock(key, handler);
                }
                //移除等待计数
                LOCK_WAIT_COUNT.remove(syncKey);
                //执行标识
                has.set(true);
                //业务处理
                return handler.get();
            } finally {
                if (has.get()) {
                    //释放分布式锁
                    releaseLock(syncKey);
                }
                LOCKS.remove(syncKey);
            }
        }
    }

    @Override
    public void syncLockSingle(@Nonnull final String key, @Nonnull final Runnable handler) {
        log.debug("syncLockSingle(key: {},handler: {})...", key, handler);
        Assert.hasText(key, "'key'不能为空!");
        final String lockKey = getSyncLockKey(key);
        Assert.hasText(lockKey, "'lockKey'不能为空!");
        synchronized (LOCKS.computeIfAbsent(lockKey, k -> new Object())) {
            try {
                //获取锁
                if (tryAcquire(lockKey, getLockTimeout())) {
                    try {
                        //执行业务
                        handler.run();
                    } finally {
                        releaseLock(lockKey);
                    }
                }
            } finally {
                LOCKS.remove(lockKey);
            }
        }
    }
}
