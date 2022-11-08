package top.zenyoung.quartz.service;

import org.springframework.boot.CommandLineRunner;
import top.zenyoung.quartz.job.TaskJob;

import javax.annotation.Nullable;

/**
 * 工作任务执行-服务接口
 *
 * @author young
 */
public interface TaskJobRunner extends CommandLineRunner, Runnable, TaskJob {

    /**
     * 执行入口
     *
     * @param args incoming main method arguments
     * @throws Exception 异常
     */
    @Override
    default void run(@Nullable final String... args) throws Exception {
        this.run();
    }
}
