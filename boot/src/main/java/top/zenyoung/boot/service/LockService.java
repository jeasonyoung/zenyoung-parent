package top.zenyoung.boot.service;

import javax.annotation.Nonnull;

/**
 * 同步锁-服务接口
 *
 * @author yangyong
 * @version 1.0
 **/
public interface LockService {

    /**
     * 同步锁业务处理
     *
     * @param key     同步锁键
     * @param handler 业务处理
     */
    void syncLock(@Nonnull final String key, @Nonnull final Runnable handler);

    /**
     * 同步互斥锁业务处理
     *
     * @param key     同步锁键
     * @param handler 业务执行
     */
    void syncLockSingle(@Nonnull final String key, @Nonnull final Runnable handler);
}
