package top.zenyoung.quartz.job;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
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
     * @param jobClass 任务实现类
     * @param jobName  任务名称
     * @param jobCron  cron表达式
     * @param args     参数集合
     */
    void addTaskJob(@Nonnull final Class<? extends BaseTaskJob> jobClass, @Nonnull final String jobName,
                    @Nonnull final String jobCron, @Nullable final Map<String, Object> args);

    /**
     * 暂停工作任务
     *
     * @param jobMaps 工作任务集合
     */
    void pauseTaskJobs(@Nonnull final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps);

    /**
     * 暂停工作任务
     *
     * @param jobClass 任务实现类
     * @param jobNames 任务名称集合
     */
    default void pauseTaskJobs(@Nonnull final Class<? extends BaseTaskJob> jobClass, @Nonnull final String... jobNames) {
        Assert.notEmpty(jobNames, "'jobNames'不能为空");
        final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps = Maps.newHashMap();
        jobMaps.put(jobClass, Lists.newArrayList(jobNames));
        pauseTaskJobs(jobMaps);
    }

    /**
     * 恢复工作任务
     *
     * @param jobMaps 工作任务集合
     */
    void resumeJobs(@Nonnull final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps);

    /**
     * 恢复工作任务
     *
     * @param jobClass 任务实现类
     * @param jobNames 任务名称集合
     */
    default void resumeJobs(@Nonnull final Class<? extends BaseTaskJob> jobClass, @Nonnull final String... jobNames) {
        Assert.notEmpty(jobNames, "'jobNames'不能为空");
        final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps = Maps.newHashMap();
        jobMaps.put(jobClass, Lists.newArrayList(jobNames));
        resumeJobs(jobMaps);
    }

    /**
     * 移除工作任务集合
     *
     * @param jobMaps 工作任务集合
     */
    void removeTaskJobs(@Nonnull final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps);

    /**
     * 移除工作任务
     *
     * @param jobClass 任务实现类
     * @param jobNames 任务名称集合
     */
    default void removeTaskJobs(@Nonnull final Class<? extends BaseTaskJob> jobClass, @Nonnull final String... jobNames) {
        Assert.notEmpty(jobNames, "'jobNames'不能为空");
        final Map<Class<? extends BaseTaskJob>, List<String>> jobMaps = Maps.newHashMap();
        jobMaps.put(jobClass, Lists.newArrayList(jobNames));
        removeTaskJobs(jobMaps);
    }
}