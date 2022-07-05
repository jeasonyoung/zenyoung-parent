package top.zenyoung.redis.service.impl;

import top.zenyoung.redis.service.TaskService;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地任务-服务接口实现
 *
 * @author yangyong
 * @version 1.0.4
 **/
public abstract class BaseLocalTaskServiceImpl extends BaseTaskServiceImpl implements TaskService {
    private final AtomicInteger refRun = new AtomicInteger(0);

    @Override
    protected final Integer getRunConcurrencyTotal(@Nonnull final String key) {
        return this.refRun.get();
    }

    @Override
    protected final void addConcurrency(@Nonnull final String key, @Nonnull final Integer increment) {
        if (Math.abs(increment) > 0) {
            this.refRun.getAndAdd(increment);
        }
    }
}
