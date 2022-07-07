package top.zenyoung.redis.lock.impl;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;
import top.zenyoung.redis.lock.LockService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁服务接口
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLockServiceImpl implements LockService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final RedissonClient client;

    @Override
    public void sync(@Nonnull final String key, @Nonnull final Duration expire,
                     @Nonnull final Runnable lockHandler, @Nullable final Runnable unlockHandler) {
        Assert.hasText(key, "'key'不能为空!");
        final String syncKey = "lock-" + key;
        synchronized (LOCKS.computeIfAbsent(syncKey, k -> new Object())) {
            try {
                //获取锁
                final RLock lock = this.client.getLock(key);
                if (lock.tryLock(expire.toMillis(), TimeUnit.MILLISECONDS)) {
                    try {
                        //业务处理
                        lockHandler.run();
                    } finally {
                        //执行完毕,解锁
                        lock.unlock();
                    }
                } else if (Objects.nonNull(unlockHandler)) {
                    //没有获得锁处理
                    unlockHandler.run();
                }
            } catch (InterruptedException e) {
                log.warn("获取同步锁失败[key:{}]-exp: {}", key, e.getMessage());
            } finally {
                LOCKS.remove(syncKey);
            }
        }
    }
}
