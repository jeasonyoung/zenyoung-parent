package top.zenyoung.redis.lock;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * 锁服务接口
 *
 * @author young
 */
public interface LockService {
    /**
     * 同步处理
     *
     * @param key     同步键
     * @param expire  同步锁有效期
     * @param handler 同步处理器
     */
    void sync(@Nonnull final String key, @Nonnull final Duration expire, @Nonnull final Runnable handler);

    /**
     * 同步处理
     *
     * @param key     同步键
     * @param handler 同步处理器
     */
    default void sync(@Nonnull final String key, @Nonnull final Runnable handler) {
        sync(key, Duration.ofSeconds(10), handler);
    }
}
