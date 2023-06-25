package top.zenyoung.segment.concurrent;

import top.zenyoung.segment.Clock;

import javax.annotation.Nonnull;

/**
 * 任务执行器
 *
 * @author young
 */
public interface AffinityJob extends Runnable {
    /**
     * 获取Job ID
     *
     * @return Job ID
     */
    String getJobId();

    default String affinity() {
        return getJobId();
    }

    /**
     * 执行饥饿策略
     */
    default void hungry() {
        setHungerTime(Clock.CACHE.secondTime());
        getPrefetchWorker().wakeup(this);
    }

    /**
     * 设置饥饿时间
     *
     * @param hungerTime 饥饿时间
     */
    void setHungerTime(final long hungerTime);

    /**
     * 获取预取器
     *
     * @return 预取器
     */
    PrefetchWorker getPrefetchWorker();

    /**
     * 设置预取器
     *
     * @param worker 预取器
     */
    void setPrefetchWorker(@Nonnull final PrefetchWorker worker);
}
