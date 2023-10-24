package top.zenyoung.quartz.job;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * 工作任务执行器基类
 *
 * @author young
 */
@Slf4j
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public abstract class BaseTaskJob implements InterruptableJob {
    private static final Map<String, Long> RUN = Maps.newConcurrentMap();

    /**
     * 任务执行入口
     *
     * @param jobName  任务名称
     * @param jobGroup 任务组名称
     * @param args     任务参数集合
     */
    protected abstract void execute(@Nonnull final String jobName, @Nonnull final String jobGroup, @Nonnull final Map<String, Object> args);

    /**
     * 任务执行入口
     *
     * @param context 任务上下文
     */
    @Override
    public final void execute(@Nonnull final JobExecutionContext context) {
        final JobKey key = context.getJobDetail().getKey();
        final String jobName = key.getName(), jobGroup = key.getGroup();
        final String lock = Joiner.on("_").join("quartz-job:", jobName, jobGroup);
        //检测业务是否在执行
        final Long val = RUN.getOrDefault(lock, 0L);
        if (Objects.nonNull(val) && val > 0) {
            return;
        }
        final Map<String, Object> args = context.getMergedJobDataMap();
        try {
            //设置已在执行标识
            RUN.put(lock, System.currentTimeMillis());
            //执行业务
            sync(lock, () -> execute(jobName, jobGroup, args));
        } finally {
            final long start = RUN.getOrDefault(lock, 0L);
            //执行完毕,移除已在执行标识
            RUN.remove(lock);
            //执行耗时
            log.info("execute-job[{}]=> 耗时: {}ms", lock, (System.currentTimeMillis() - start));
        }
    }

    protected void sync(@Nonnull final String key, @Nonnull final Runnable handler) {
        log.info("sync(key: {}) => {}", key, handler);
        handler.run();
    }

    @Override
    public void interrupt() {
        log.warn("interrupt.");
    }
}
