package top.zenyoung.framework.runtime.service.impl;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import top.zenyoung.common.util.LocalSyncUtils;
import top.zenyoung.framework.Constants;
import top.zenyoung.framework.service.RedisEnhancedService;
import top.zenyoung.common.util.BeanCacheUtils;
import top.zenyoung.service.SyncLockService;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis同步锁-服务接口实现
 *
 * @author yangyong
 * @version 1.0
 **/
@Slf4j
@RequiredArgsConstructor
public class RedisSyncLockServiceImpl implements SyncLockService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private final RedissonClient client;
    private final ApplicationContext context;

    private void redisHandler(@Nonnull final Runnable handler) {
        BeanCacheUtils.consumer(context, RedisEnhancedService.class, bean -> bean.redisHandler(handler));
    }

    /**
     * 获取同步锁键
     *
     * @param key 键名
     * @return 同步锁键
     */
    protected String getSyncLockKey(@Nonnull final String key) {
        return Constants.PREFIX + "lock" + Constants.SEP_REDIS + key;
    }

    @Override
    public void syncLock(@Nonnull final String key, @Nonnull final Runnable handler) {
        log.debug("syncLock(key: {},handler: {})...", key, handler);
        Assert.hasText(key, "'key'不能为空!");
        final String lockKey = getSyncLockKey(key);
        LocalSyncUtils.syncHandler(LOCKS, lockKey, () -> redisHandler(() -> {
            final RLock lock = client.getLock(lockKey);
            try {
                handler.run();
            } finally {
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }
        }));
    }

    @Override
    public void syncLockSingle(@Nonnull final String key, @Nonnull final Runnable handler) {
        log.debug("syncLockSingle(key: {},handler: {})...", key, handler);
        Assert.hasText(key, "'key'不能为空!");
        final String lockKey = getSyncLockKey(key);
        LocalSyncUtils.syncHandler(LOCKS, lockKey, () -> redisHandler(() -> {
            final RLock lock = client.getLock(lockKey);
            try {
                //获取锁
                if (lock.tryLock(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                    try {
                        //执行业务
                        handler.run();
                    } finally {
                        if (lock.isLocked()) {
                            lock.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.warn("syncLockSingle(key: {},handler: {})-exp: {}", key, handler, e.getMessage());
            }
        }));
    }
}
