package top.zenyoung.redis.service.impl;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.util.Assert;
import top.zenyoung.redis.service.QueueService;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Redis队列-服务接口实现
 *
 * @author yangyong
 * @version 1.0
 **/
@Slf4j
@RequiredArgsConstructor
public class RedisQueueServiceImpl implements QueueService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final RedissonClient redissonClient;

    /**
     * 获取Redis队列键
     *
     * @param key 队列键
     * @return Redis队列键
     */
    @Nonnull
    protected String getRedisQueueKey(@Nonnull final String key) {
        return "zy-queue:" + key;
    }

    @Override
    public <T extends Serializable> void pushQueue(@Nonnull final String key, @Nonnull final T data) {
        log.debug("pushQueue(key: {},data: {})...", key, data);
        Assert.hasText(key, "'key'不能为空!");
        //队列键
        final String queueKey = getRedisQueueKey(key);
        Assert.hasText(queueKey, "'queueKey'不能为空!");
        //入队处理
        final String lock = queueKey + "-push";
        synchronized (LOCKS.computeIfAbsent(lock, k -> new Object())) {
            try {
                this.redissonClient.getQueue(queueKey).add(data);
            } catch (Throwable ex) {
                log.warn("pushQueue(key: {},data: {})-exp: {}", key, data, ex.getMessage());
                throw new RuntimeException(ex);
            } finally {
                LOCKS.remove(lock);
            }
        }
    }

    @Override
    public <T extends Serializable> int popQueue(@Nonnull final String key, @Nonnull final Class<T> dataClass, @Nonnull final Consumer<T> consumer) {
        log.debug("popQueue(key: {},dataClass: {},consumer: {})...", key, dataClass, consumer);
        Assert.hasText(key, "'key'不能为空!");
        //队列键
        final String queueKey = getRedisQueueKey(key);
        Assert.hasText(queueKey, "'queueKey'不能为空!");
        //出队处理
        final AtomicInteger refCounts = new AtomicInteger(0);
        final String lock = queueKey + "-pop";
        synchronized (LOCKS.computeIfAbsent(lock, k -> new Object())) {
            try {
                //出队处理
                final RQueue<T> queue = this.redissonClient.getQueue(queueKey);
                T data;
                while (Objects.nonNull(data = queue.poll())) {
                    //计数器累加
                    refCounts.incrementAndGet();
                    //业务处理
                    consumer.accept(data);
                }
            } catch (QueryTimeoutException ex) {
                log.debug("popQueue(key: {},dataClass: {},consumer: {})-exp: {}", key, dataClass, consumer, ex.getMessage());
            } catch (Throwable ex) {
                log.warn("popQueue(key: {},dataClass: {},consumer: {})-exp: {}", key, dataClass, consumer, ex.getMessage());
                throw new RuntimeException(ex);
            } finally {
                LOCKS.remove(lock);
            }
        }
        return refCounts.get();
    }
}
