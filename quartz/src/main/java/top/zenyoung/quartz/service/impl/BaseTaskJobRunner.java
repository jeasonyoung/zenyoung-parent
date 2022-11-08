package top.zenyoung.quartz.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import top.zenyoung.quartz.job.BaseTaskJob;
import top.zenyoung.quartz.job.TaskJobManager;
import top.zenyoung.quartz.service.TaskJobRunner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 工作任务执行-服务接口实现基类
 *
 * @author young
 */
@Slf4j
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public abstract class BaseTaskJobRunner extends BaseTaskJob implements TaskJobRunner {
    /**
     * 工作任务管理器
     */
    @Autowired
    private TaskJobManager jobManager;

    /**
     * 获取默认工作任务名称
     *
     * @return 默认名称
     */
    protected String getDefaultJobName() {
        return getClass().getSimpleName();
    }

    /**
     * 添加工作任务
     *
     * @param jobName 任务名称
     * @param jobCron cron表达式
     * @param args    参数集合
     */
    protected void addTaskJob(@Nonnull final String jobName, @Nonnull final String jobCron, @Nullable final Map<String, Object> args) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobCron, "'jobCron'不能为空");
        this.jobManager.addTaskJob(getClass(), jobName, jobCron, args);
    }

    /**
     * 添加工作任务
     *
     * @param jobName 任务名称
     * @param jobCron cron表达式
     */
    protected void addTaskJob(@Nonnull final String jobName, @Nonnull final String jobCron) {
        addTaskJob(jobName, jobCron, null);
    }

    /**
     * 添加工作任务
     *
     * @param jobCron cron表达式
     */
    protected void addTaskJob(@Nonnull final String jobCron) {
        addTaskJob(getDefaultJobName(), jobCron, null);
    }

    /**
     * 暂停工作任务
     *
     * @param jobNames 任务名称集合
     */
    protected void pauseTaskJobs(@Nonnull final String... jobNames) {
        Assert.notEmpty(jobNames, "'jobNames'不能为空");
        this.jobManager.pauseTaskJobs(getClass(), jobNames);
    }

    /**
     * 暂停工作任务
     */
    protected void pauseTaskJobs() {
        this.pauseTaskJobs(getDefaultJobName());
    }

    /**
     * 恢复工作任务
     *
     * @param jobNames 任务名称集合
     */
    protected void resumeJobs(@Nonnull final String... jobNames) {
        Assert.notEmpty(jobNames, "'jobNames'不能为空");
        this.jobManager.resumeJobs(getClass(), jobNames);
    }

    /**
     * 恢复工作任务
     */
    protected void resumeJobs() {
        resumeJobs(getDefaultJobName());
    }

    /**
     * 移除工作任务
     *
     * @param jobNames 任务名称集合
     */
    protected void removeTaskJobs(@Nonnull final String... jobNames) {
        Assert.notEmpty(jobNames, "'jobNames'不能为空");
        this.jobManager.removeTaskJobs(getClass(), jobNames);
    }

    /**
     * 移除工作任务
     */
    protected void removeTaskJobs() {
        removeTaskJobs(getDefaultJobName());
    }
}
