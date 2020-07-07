package top.zenyoung.service.impl;

import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.service.TaskService;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务-服务接口实现
 *
 * @author yangyong
 * @version 1.0.4
 **/
@Slf4j
public abstract class AbstractTaskServiceImpl implements TaskService {
    private static final Cache<String, Integer> MAX_TASK_CACHE = CacheUtils.createCache(100, 30, TimeUnit.SECONDS);
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<String, AtomicInteger> TASK_COUTERS = Maps.newConcurrentMap();

    /**
     * 默认支持的最大并发数(CPU内核数)
     */
    protected static final int DEF_MAX = Runtime.getRuntime().availableProcessors();

    /**
     * 获取任务最大并发数
     *
     * @param key 任务键
     * @return 最大并发数
     */
    @Nonnull
    protected abstract Integer getMaxTaskConcurrents(@Nonnull final String key);

    private int getMaxConcurrents(@Nonnull final String key) {
        final String lock = key + "-max_task";
        synchronized (LOCKS.computeIfAbsent(lock, k -> new Object())) {
            try {
                final Integer val = CacheUtils.getCacheValue(MAX_TASK_CACHE, key, () -> getMaxTaskConcurrents(key));
                return (val == null || val <= 0) ? DEF_MAX : val;
            } finally {
                LOCKS.remove(lock);
            }
        }
    }

    @Override
    public void startTask(@Nonnull final String key, @Nonnull final Runnable process) {
        log.debug("startTask(key: {},process: {})...", key, process);
        Assert.hasText(key, "'key'不能为空!");
        //最大并发数
        final int max = getMaxConcurrents(key);
        //获取当前计数器
        final AtomicInteger refCounter = TASK_COUTERS.computeIfAbsent(key, k -> new AtomicInteger(0));
        if (refCounter.get() >= max) {
            log.warn("startTask(key: {},process: {})-并行超过阀值[{}/{}],请求稍后", key, process, refCounter, max);
            return;
        }
        //计数器累加
        refCounter.incrementAndGet();
        try {
            //执行业务
            process.run();
        } finally {
            //计数器递减
            refCounter.decrementAndGet();
        }
    }
}
