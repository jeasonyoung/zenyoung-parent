package top.zenyoung.redis.service.impl;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.redis.service.TaskService;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 任务基础实现类
 *
 * @author young
 */
@Slf4j
abstract class BaseTaskServiceImpl implements TaskService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();

    /**
     * 获取当前执行并发数
     *
     * @param key 执行键
     * @return 当前执行并发数
     */
    protected abstract Integer getRunConcurrencyTotal(@Nonnull final String key);

    /**
     * 增加执行并发数
     *
     * @param key       执行键
     * @param increment 增加数量
     */
    protected abstract void addConcurrency(@Nonnull final String key, @Nonnull final Integer increment);

    /**
     * 获取最大并发数
     *
     * @return 最大并发数
     */
    protected Integer getMaxConcurrencyTotal() {
        return 1;
    }

    @Override
    public final void startTask(@Nonnull final String key, @Nonnull final Runnable process) {
        log.debug("startTask(key: {},process: {})...", key, process);
        final String lockKey = key + ":lock";
        synchronized (LOCKS.computeIfAbsent(lockKey, k -> new Object())) {
            try {
                final int max = this.getMaxConcurrencyTotal();
                final int run = this.getRunConcurrencyTotal(key);
                if (run >= max) {
                    log.debug("任务正在执行中,等待处理完成[{}/{}]", run, max);
                    return;
                }
                //计数器累加
                this.addConcurrency(key, 1);
                final long start = System.currentTimeMillis();
                try {
                    //执行任务
                    process.run();
                } finally {
                    //计数器递减
                    this.addConcurrency(key, -1);
                    log.info("任务执行耗时: {}ms", (System.currentTimeMillis() - start));
                }
            } finally {
                LOCKS.remove(lockKey);
            }
        }
    }
}
