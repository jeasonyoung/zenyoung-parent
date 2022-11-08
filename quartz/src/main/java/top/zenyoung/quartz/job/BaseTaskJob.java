package top.zenyoung.quartz.job;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import top.zenyoung.boot.service.impl.BaseServiceImpl;
import top.zenyoung.redis.lock.LockService;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * 工作任务接口
 *
 * @author young
 */
@Slf4j
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public abstract class BaseTaskJob extends BaseServiceImpl implements TaskJob {
    private static final Map<String, Object> RUN = Maps.newConcurrentMap();
    @Autowired
    private LockService lockService;

    /**
     * 任务执行业务入口
     *
     * @param jobName 工作任务名称
     * @param args    工作参数
     */
    protected abstract void execute(@Nonnull final String jobName, @Nonnull final Map<String, Object> args);

    /**
     * 任务执行入口
     *
     * @param context 任务上下文
     */
    @Override
    public final void execute(@Nonnull final JobExecutionContext context) {
        final JobKey key = context.getJobDetail().getKey();
        final Map<String, Object> args = context.getMergedJobDataMap();
        final String jobName = key.getName(), jobGroupName = key.getGroup();
        final String lock = "quartz-job:" + jobName + "_" + jobGroupName;
        synchronized (this) {
            //检测业务是否在执行
            final Object o = RUN.getOrDefault(lock, null);
            if (Objects.nonNull(o)) {
                return;
            }
            try {
                //设置已在执行标识
                RUN.put(lock, new Object());
                //执行业务
                if (Objects.nonNull(lockService)) {
                    lockService.sync(lock, () -> execute(jobName, args));
                } else {
                    this.execute(jobName, args);
                }
            } catch (Throwable e) {
                log.warn("execute(jobName: {},args: {})-exp: {}", jobName, args, e.getMessage());
            } finally {
                //执行完毕,移除已在执行标识
                RUN.remove(lock);
            }
        }
    }
}
