package top.zenyoung.redis.service;

import javax.annotation.Nonnull;

/**
 * 任务-服务接口
 *
 * @author yangyong
 * @version 1.0.4
 **/
public interface TaskService {

    /**
     * 启动任务
     *
     * @param key     任务键
     * @param process 任务业务处理
     */
    void startTask(@Nonnull final String key, @Nonnull final Runnable process);

    /**
     * 任务线程等待
     *
     * @param millis 等待时间(毫秒)
     */
    default void taskThradWait(final long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
