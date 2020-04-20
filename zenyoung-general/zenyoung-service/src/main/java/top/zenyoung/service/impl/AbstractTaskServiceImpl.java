package top.zenyoung.service.impl;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import top.zenyoung.service.TaskService;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务-服务接口实现
 *
 * @author yangyong
 * @version 1.0.4
 **/
@Slf4j
public abstract class AbstractTaskServiceImpl implements TaskService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<String, AtomicInteger> TASK_CONCURRENCY_TOTALS = Maps.newConcurrentMap();

    private static final int MAX = Runtime.getRuntime().availableProcessors() * 10;

    /**
     * 获取任务最大并发数
     *
     * @param key 任务键
     * @return 最大并发数
     */
    protected int getTaskMaxConcurrent(@Nonnull final String key) {
        log.debug("getTaskMaxConcurrent(key: {})...", key);
        return MAX;
    }

    @Override
    public void startTask(@Nonnull final String key, @Nonnull final Runnable process) {
        log.debug("startTask(key: {},process: {})...", key, process);
        Assert.hasText(key, "'key'不能为空!");
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                //并行阀值
                final int max = getTaskMaxConcurrent(key);
                //检查并行是否超过阀值
                final AtomicInteger refCounter = TASK_CONCURRENCY_TOTALS.computeIfAbsent(key, k -> new AtomicInteger(0));
                if (refCounter.get() > MAX) {
                    log.warn("startTask(key: {},process: {})-并行超过阀值[{}/{}],请求稍后", key, process, refCounter, max);
                    return;
                }
                //计数器累加
                refCounter.incrementAndGet();
                //执行任务
                try {
                    process.run();
                } finally {
                    //计数器递减
                    refCounter.decrementAndGet();
                }
            } finally {
                LOCKS.remove(key);
            }
        }
    }
}
