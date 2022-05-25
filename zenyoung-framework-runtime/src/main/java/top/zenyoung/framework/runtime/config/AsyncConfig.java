package top.zenyoung.framework.runtime.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置
 *
 * @author young
 */
@EnableAsync
@Configuration
@ConditionalOnMissingBean(AsyncConfigurer.class)
public class AsyncConfig implements AsyncConfigurer {
    /**
     * 核心线程数
     */
    private static final int CORE_POOLS_SIZE = 10;
    /**
     * 最大线程数
     */
    private static final int MAX_POOLS_SIZE = 50;

    @Override
    public Executor getAsyncExecutor() {
        //使用Spring内置线程池任务对象
        final ThreadPoolTaskExecutor taskScheduler = new ThreadPoolTaskExecutor();
        //设置线程参数
        taskScheduler.setBeanName("task-async");
        taskScheduler.setCorePoolSize(CORE_POOLS_SIZE);
        taskScheduler.setMaxPoolSize(MAX_POOLS_SIZE);
        taskScheduler.setQueueCapacity(Integer.MAX_VALUE);
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        taskScheduler.initialize();
        //返回线程池
        return taskScheduler;
    }
}
