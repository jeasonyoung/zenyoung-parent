package top.zenyoung.quartz.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 工作任务管理器工厂
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class TaskJobManagerFactory implements TaskJobManager {
    private final Scheduler scheduler;

    private JobKey createJobKey(@Nonnull final String jobName, @Nonnull final String jobGroup) {
        return JobKey.jobKey(jobName, jobGroup);
    }

    @Override
    public void saveTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup, @Nonnull final String jobCron,
                            @Nonnull final Class<? extends BaseTaskJob> jobClass, @Nullable final Map<String, Object> args) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        Assert.hasText(jobCron, "'jobCron'不能为空");
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            if (!CollectionUtils.isEmpty(args)) {
                jobDataMap.putAll(args);
            }
            final TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            //不存在则新增
            if (trigger == null) {
                final JobKey jobKey = createJobKey(jobName, jobGroup);
                final JobDetail jobDetail = JobBuilder.newJob(jobClass)
                        .withIdentity(jobKey)
                        .usingJobData(jobDataMap)
                        .build();
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey)
                        .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                        .withSchedule(CronScheduleBuilder.cronSchedule(jobCron))
                        .startNow()
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
                if (!scheduler.isShutdown()) {
                    scheduler.start();
                }
                log.info("saveTaskJob(jobName: {},jobGroup: {},jobCron: {},jobClass: {},args: {})-已添加",
                        jobName, jobGroup, jobCron, jobClass, args);
                return;
            }
            //检查定时表达式
            if (jobCron.equalsIgnoreCase(trigger.getCronExpression())) {
                log.info("saveTaskJob(jobName: {},jobGroup: {},jobCron: {},jobClass: {},args: {})-已存在",
                        jobName, jobGroup, jobCron, jobClass, args);
                return;
            }
            //更新处理
            trigger = trigger.getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobCron))
                    .usingJobData(jobDataMap)
                    .build();
            //重启触发器
            scheduler.rescheduleJob(triggerKey, trigger);
            log.info("saveTaskJob(jobName: {},jobGroup: {},jobCron: {},jobClass: {},args: {})-已重启",
                    jobName, jobGroup, jobCron, jobClass, args);
        } catch (SchedulerException e) {
            log.error("saveTaskJob(jobName: {},jobGroup: {},jobCron: {},jobClass: {},args: {})-exp: {}",
                    jobName, jobGroup, jobCron, jobClass, args, e.getMessage());
        }
    }

    @Override
    public void pauseTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        final JobKey jobKey = createJobKey(jobName, jobGroup);
        try {
            scheduler.pauseJob(jobKey);
            log.info("pauseTaskJob(jobName: {},jobGroup: {})[{}]-已暂停", jobName, jobGroup, jobKey);
        } catch (SchedulerException e) {
            log.error("pauseTaskJob(jobName: {},jobGroup: {})[{}]-exp: {}", jobName, jobGroup, jobKey, e.getMessage());
        }
    }

    @Override
    public void resumeJob(@Nonnull final String jobName, @Nonnull final String jobGroup) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        final JobKey jobKey = createJobKey(jobName, jobGroup);
        try {
            scheduler.resumeJob(jobKey);
            log.info("resumeJob(jobName: {},jobGroup: {})[{}]-已恢复", jobName, jobGroup, jobKey);
        } catch (SchedulerException e) {
            log.info("resumeJob(jobName: {},jobGroup: {})[{}]-exp: {}", jobName, jobGroup, jobKey, e.getMessage());
        }
    }

    @Override
    public void removeTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobGroup, "'jobGroup'不能为空");
        final JobKey jobKey = createJobKey(jobName, jobGroup);
        try {
            scheduler.deleteJob(jobKey);
            log.info("removeTaskJob(jobName: {},jobGroup: {})[{}]-已删除", jobName, jobGroup, jobKey);
        } catch (Throwable e) {
            log.error("removeTaskJob(jobName:{},jobGroup: {})[{}]-exp: {}", jobName, jobGroup, jobKey, e.getMessage());
        }
    }
}
