package top.zenyoung.redis.lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * @param key           同步键
     * @param expire        同步锁有效期
     * @param lockHandler   锁同步处理器
     * @param unlockHandler 无锁处理器
     */
    void sync(@Nonnull final String key, @Nonnull final Duration expire, @Nonnull final Runnable lockHandler, @Nullable final Runnable unlockHandler);

    /**
     * 同步处理
     *
     * @param key     同步键
     * @param expire  同步锁有效期
     * @param handler 同步处理器
     */
    default void sync(@Nonnull final String key, @Nonnull final Duration expire, @Nonnull final Runnable handler) {
        sync(key, expire, handler, null);
    }

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
