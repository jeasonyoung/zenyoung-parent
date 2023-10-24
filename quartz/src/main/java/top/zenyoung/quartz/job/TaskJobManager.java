package top.zenyoung.quartz.job;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * 工作任务管理器接口
 *
 * @author young
 */
public interface TaskJobManager {
    /**
     * 增加一个工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务分组名称
     * @param jobCron  cron表达式
     * @param jobClass 任务实现类
     * @param args     参数集合
     */
    void saveTaskJob(@Nonnull final String jobName,
                     @Nonnull final String jobGroup,
                     @Nonnull final String jobCron,
                     @Nonnull final Class<? extends BaseTaskJob> jobClass,
                     @Nullable final Map<String, Object> args);

    /**
     * 暂停工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务分组名称
     */
    void pauseTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup);

    /**
     * 恢复工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务分组名称
     */
    void resumeJob(@Nonnull final String jobName, @Nonnull final String jobGroup);

    /**
     * 移除工作任务
     *
     * @param jobName  任务名称
     * @param jobGroup 任务分组名称
     */
    void removeTaskJob(@Nonnull final String jobName, @Nonnull final String jobGroup);
}