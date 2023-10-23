package top.zenyoung.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.quartz.job.TaskJobManager;
import top.zenyoung.quartz.job.TaskJobManagerFactory;

import javax.annotation.Nonnull;

/**
 * 定时任务-自动配置
 *
 * @author young
 */
@Slf4j
@Configuration
public class QuartzAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TaskJobManager taskJobManager(@Nonnull final ObjectProvider<Scheduler> provider) {
        return TaskJobManagerFactory.of(provider.getIfAvailable());
    }
}