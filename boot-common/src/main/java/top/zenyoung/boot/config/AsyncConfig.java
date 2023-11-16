package top.zenyoung.boot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.task.TaskExecutor;
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
@Slf4j
@EnableAsync
@Configuration
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

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.warn("async[method:{}, params: {}]-exp: {}", method.getName(), params, ex.getMessage());
    }

    /**
     * 获取异步事件线程池
     *
     * @return 异步事件线程池
     */
    @ConditionalOnMissingBean
    @Bean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public SimpleApplicationEventMulticaster asyncMulticaster() {
        log.info("asyncMulticaster....");
        final SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
        multicaster.setTaskExecutor(taskExecutor());
        return multicaster;
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutor taskExecutor() {
        final int cpus = Runtime.getRuntime().availableProcessors();
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(cpus, 5));
        executor.setMaxPoolSize(Math.max(cpus * 3, 20));
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("thread-event-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
