package top.zenyoung.segment.concurrent;

import javax.annotation.Nonnull;

/**
 * 预取器
 *
 * @author young
 */
public interface PrefetchWorker {
    /**
     * 获取预取器名称
     *
     * @return 预取器名称
     */
    String getName();

    /**
     * 执行任务
     *
     * @param job 任务执行器
     */
    void submit(@Nonnull final AffinityJob job);

    /**
     * 取消任务
     *
     * @param job 任务执行器
     */
    void cancel(@Nonnull final AffinityJob job);

    /**
     * 唤醒任务
     *
     * @param job 任务执行器
     */
    void wakeup(@Nonnull final AffinityJob job);

    /**
     * 关闭
     */
    void shutdown();
}
