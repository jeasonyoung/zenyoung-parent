package top.zenyoung.quartz.job;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工作任务管理器工厂
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class TaskJobManagerFactory implements TaskJobManager {
    private final Scheduler scheduler;

    @Override
    public void addTaskJob(@Nonnull final Class<? extends BaseTaskJob> jobClass, @Nonnull final String jobName,
                           @Nonnull final String jobCron, @Nullable final Map<String, Object> args) {
        Assert.hasText(jobName, "'jobName'不能为空");
        Assert.hasText(jobCron, "'jobCron'不能为空");
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            if (!CollectionUtils.isEmpty(args)) {
                jobDataMap.putAll(args);
            }
            final String jobGroupName = jobClass.getName();
            final TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            //不存在则新增
            if (Objects.isNull(trigger)) {
                final JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).usingJobData(jobDataMap).build();
                trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroupName)
                        .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                        .withSchedule(CronScheduleBuilder.cronSchedule(jobCron)).startNow().build();
                scheduler.scheduleJob(jobDetail, trigger);
                if (!scheduler.isShutdown()) {
                    scheduler.start();
                }
                log.info("addTaskJob(jobClass: {},jobName: {},jobCron: {},args: {})-已添加", jobClass, jobName, jobCron, args);
                return;
            }
            //检查定时表达式
            if (jobCron.equalsIgnoreCase(trigger.getCronExpression())) {
                log.info("addTaskJob(jobClass: {},jobName: {},jobCron: {},args: {})-已存在", jobClass, jobName, jobCron, args);
                return;
            }
            //更新处理
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobCron)).usingJobData(jobDataMap).build();
            //重启触发器
            scheduler.rescheduleJob(triggerKey, trigger);
            log.info("addTaskJob(jobClass: {},jobName: {},jobCron: {},args: {})-已重启", jobClass, jobName, jobCron, args);
        } catch (Throwable e) {
            log.error("addTaskJob(jobClass: {},jobName: {},jobCron: {})-exp: {}", jobClass, jobName, jobCron, e.getMessage());
        }
    }

    private List<JobKey> getJobKeys(@Nonnull final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps) {
        return jobMaps.entrySet().stream()
                .map(entry -> {
                    final Class<? extends BaseTaskJob> jobClass = entry.getKey();
                    final List<String> jobNames = entry.getValue();
                    if (!CollectionUtils.isEmpty(jobNames)) {
                        final String jobGroupName = jobClass.getSimpleName();
                        return jobNames.stream().distinct()
                                .filter(jobName -> !Strings.isNullOrEmpty(jobName))
                                .map(jobName -> JobKey.jobKey(jobName, jobGroupName))
                                .collect(Collectors.toList());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void pauseTaskJobs(@Nonnull final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps) {
        Assert.notEmpty(jobMaps, "'jobMaps'不能为空");
        final List<JobKey> jobKeys = getJobKeys(jobMaps);
        if (!CollectionUtils.isEmpty(jobKeys)) {
            jobKeys.forEach(jobKey -> {
                try {
                    scheduler.pauseJob(jobKey);
                    log.info("pauseJob(jobKey: {})-已暂停", jobKey);
                } catch (SchedulerException e) {
                    log.error("pauseJob(jobKey: {})-exp: {}", jobKey, e.getMessage());
                }
            });
        }
    }

    @Override
    public void resumeJobs(@Nonnull final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps) {
        Assert.notEmpty(jobMaps, "'jobMaps'不能为空");
        final List<JobKey> jobKeys = getJobKeys(jobMaps);
        if (!CollectionUtils.isEmpty(jobKeys)) {
            jobKeys.forEach(jobKey -> {
                try {
                    scheduler.resumeJob(jobKey);
                    log.info("resumeJob(jobKey: {})-已恢复", jobKey);
                } catch (SchedulerException e) {
                    log.error("resumeJob(jobKey: {})-exp: {}", jobKey, e.getMessage());
                }
            });
        }
    }

    @Override
    public void removeTaskJobs(@Nonnull final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps) {
        Assert.notEmpty(jobMaps, "'jobMaps'不能为空");
        try {
            final List<JobKey> jobKeys = getJobKeys(jobMaps);
            if (!CollectionUtils.isEmpty(jobKeys)) {
                scheduler.deleteJobs(jobKeys);
                log.info("deleteJobs(jobKeys: {})-已删除", jobKeys);
            }
        } catch (Throwable e) {
            log.error("removeTaskJobs(jobMaps:{})-exp: {}", jobMaps, e.getMessage());
        }
    }
}
