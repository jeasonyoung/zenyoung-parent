package top.zenyoung.boot.service.impl;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import top.zenyoung.boot.service.TaskService;

import javax.annotation.Nonnull;

/**
 * 分布式任务-服务接口实现
 *
 * @author young
 */
public class BaseDistributedTaskServiceImpl extends BaseTaskServiceImpl implements TaskService {

    @Autowired
    private RedissonClient client;

    private String getRedisKey(@Nonnull final String key) {
        return "zy-task:" + key;
    }

    @Override
    protected final Integer getRunConcurrencyTotal(@Nonnull final String key) {
        Assert.hasText(key, "'key'不能为空!");
        return (int) client.getAtomicLong(getRedisKey(key)).get();
    }

    @Override
    protected final void addConcurrency(@Nonnull final String key, @Nonnull final Integer increment) {
        Assert.hasText(key, "'key'不能为空!");
        if (Math.abs(increment) > 0) {
            client.getAtomicLong(getRedisKey(key)).getAndAdd(increment);
        }
    }
}
