package top.zenyoung.quartz.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.util.Assert;
import top.zenyoung.quartz.job.BaseTaskJob;
import top.zenyoung.quartz.job.TaskJobManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 工作任务执行-服务接口实现基类
 *
 * @author young
 */
@Slf4j
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public abstract class BaseTaskJobRunner extends BaseTaskJob implements CommandLineRunner {
    /**
     * 工作任务管理器
     */
    @Autowired
    private TaskJobManager jobManager;

    /**
     * 任务管理器处理
     *
     * @param handler 业务处理实现
     */
    protected void jobManagerHandler(@Nonnull final Consumer<TaskJobManager> handler) {
        Assert.notNull(jobManager, "'jobManager'不能为空");
        handler.accept(jobManager);
    }

    /**
     * 添加工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务组名称
     * @param jobCron  cron表达式
     * @param args     参数集合
     */
    protected void saveTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup,
                               @Nonnull final String jobCron, @Nullable final Map<String, Object> args) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        Assert.hasText(jobCron, "'jobCron'不能为空");
        jobManagerHandler(jm -> jm.saveTaskJob(jobName, jobGroup, jobCron, getClass(), args));
    }

    /**
     * 添加工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务组名称
     * @param jobCron  cron表达式
     */
    protected void saveTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup, @Nonnull final String jobCron) {
        saveTaskJob(jobName, jobGroup, jobCron, null);
    }


    /**
     * 暂停工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务组名称
     */
    protected void pauseTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        jobManagerHandler(jm -> jm.pauseTaskJob(jobName, jobGroup));
    }

    /**
     * 恢复工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务组名称
     */
    protected void resumeJob(@Nonnull final String jobName, @Nonnull final String jobGroup) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        jobManagerHandler(jm -> jm.resumeJob(jobName, jobGroup));
    }

    /**
     * 移除工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务组名称
     */
    protected void removeTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        jobManagerHandler(jm -> jm.removeTaskJob(jobName, jobGroup));
    }
}
