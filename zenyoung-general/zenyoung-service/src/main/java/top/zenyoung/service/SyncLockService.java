package top.zenyoung.service;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * 同步锁-服务接口
 *
 * @author yangyong
 * @version 1.0
 **/
public interface SyncLockService {

    /**
     * 同步锁业务处理
     *
     * @param key     同步锁键
     * @param handler 业务处理
     * @param <T>     结果数据类型
     * @return 结果数据
     */
    <T> T syncLock(@Nonnull final String key, @Nonnull final Supplier<T> handler);

    /**
     * 同步互斥锁业务处理
     *
     * @param key     同步锁键
     * @param timeout 超时时间
     * @param handler 业务执行
     */
    void syncLockSingle(@Nonnull final String key, @Nonnull final Duration timeout, @Nonnull final Runnable handler);
}
