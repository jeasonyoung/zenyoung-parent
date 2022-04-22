package top.zenyoung.framework.runtime.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.framework.Constants;
import top.zenyoung.service.QueueService;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Redis队列-服务接口实现
 *
 * @author yangyong
 * @version 1.0
 **/
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RedisQueueServiceImpl implements QueueService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final long QUEUE_READ_TIMEOUT = 10000;
    private static final int QUEUE_READ_MAX = Runtime.getRuntime().availableProcessors() * 2;

    private final ObjectMapper objMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 获取Redis队列键
     *
     * @param key 队列键
     * @return Redis队列键
     */
    @Nonnull
    protected String getRedisQueueKey(@Nonnull final String key) {
        return Constants.PREFIX + "queue" + Constants.SEP_REDIS + key;
    }

    @Override
    public <T extends Serializable> void pushQueue(@Nonnull final String key, @Nonnull final T data) {
        log.debug("pushQueue(key: {},data: {})...", key, data);
        Assert.hasText(key, "'key'不能为空!");
        //队列键
        final String queueKey = getRedisQueueKey(key);
        Assert.hasText(queueKey, "'queueKey'不能为空!");
        //入队处理
        synchronized (LOCKS.computeIfAbsent(queueKey + "-push", k -> new Object())) {
            try {
                //序列化数据
                final String json = JsonUtils.toJson(objMapper, data);
                if (!Strings.isNullOrEmpty(json)) {
                    //数据入队
                    final Long ret = redisTemplate.opsForList().leftPush(queueKey, json);
                    log.info("pushQueue(key: {},data: {})=> {}", key, data, ret);
                }
            } catch (Throwable ex) {
                log.warn("pushQueue(key: {},data: {})-exp: {}", key, data, ex.getMessage());
                throw new RuntimeException(ex);
            } finally {
                LOCKS.remove(queueKey + "-push");
            }
        }
    }

    /**
     * 获取读取超时时间(毫秒)
     *
     * @return 读取超时时间(毫秒)
     */
    @Nonnull
    protected Long getReadTimeout() {
        return QUEUE_READ_TIMEOUT;
    }

    /**
     * 获取读取最大数据量
     *
     * @return 读取最大数据量
     */
    @Nonnull
    protected Integer getReadMax() {
        return QUEUE_READ_MAX;
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
        synchronized (LOCKS.computeIfAbsent(queueKey + "-pop", k -> new Object())) {
            try {
                //出队最大数据量
                int max = getReadMax();
                if (max <= 0) {
                    max = QUEUE_READ_MAX;
                }
                //出队超时时间
                long timeout = getReadTimeout();
                if (timeout <= 0) {
                    timeout = QUEUE_READ_TIMEOUT;
                }
                //出队处理
                final ListOperations<String, String> queue = redisTemplate.opsForList();
                String json;
                int count = 0;
                while ((count < max) && !Strings.isNullOrEmpty(json = queue.rightPop(queueKey, timeout, TimeUnit.MILLISECONDS))) {
                    //计数器累加
                    count = refCounts.incrementAndGet();
                    //业务处理
                    final T data = JsonUtils.fromJson(objMapper, json, dataClass);
                    if (data != null) {
                        consumer.accept(data);
                    }
                }
            } catch (QueryTimeoutException ex) {
                log.debug("popQueue(key: {},dataClass: {},consumer: {})-exp: {}", key, dataClass, consumer, ex.getMessage());
            } catch (Throwable ex) {
                log.warn("popQueue(key: {},dataClass: {},consumer: {})-exp: {}", key, dataClass, consumer, ex.getMessage());
                throw new RuntimeException(ex);
            } finally {
                LOCKS.remove(queueKey + "-pop");
            }
        }
        return refCounts.get();
    }
}
