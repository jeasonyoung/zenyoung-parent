package top.zenyoung.monitor;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 事件服务基类
 *
 * @author yangyong
 * @version 1.0
 * date 2020/7/7 5:37 下午
 **/
@Slf4j
public abstract class BaseOnEventService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();

    /**
     * 创建锁定键前缀
     *
     * @return 锁定键前缀
     */
    @Nonnull
    protected abstract String createLockKeyPrefix();

    /**
     * 获取完整锁定键
     *
     * @param lockKey 锁定键
     * @return 完整锁定键
     */
    protected String getFullLockKey(@Nonnull final String lockKey) {
        final String prefix = createLockKeyPrefix();
        return (Strings.isNullOrEmpty(prefix) ? this.getClass().getName() : prefix) + ":" + lockKey;
    }

    /**
     * 触发事件
     *
     * @param lockKey 锁定键
     * @param handler 业务处理
     */
    protected void onEventHandler(@Nonnull final String lockKey, @Nonnull final Runnable handler) {
        log.debug("onEventHandler(lockKey: {},handler: {})...", lockKey, handler);
        Assert.hasText(lockKey, "'lockKey'不能为空!");
        final String key = getFullLockKey(lockKey);
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                //业务处理
                handler.run();
            } finally {
                LOCKS.remove(key);
            }
        }
    }

}
