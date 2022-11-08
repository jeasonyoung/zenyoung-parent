package top.zenyoung.quartz.job;

import org.quartz.InterruptableJob;

/**
 * 工作任务接口
 *
 * @author young
 */
public interface TaskJob extends InterruptableJob {

    /**
     * 中断处理
     */
    @Override
    default void interrupt() {

    }
}
