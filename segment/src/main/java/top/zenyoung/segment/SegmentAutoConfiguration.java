package top.zenyoung.segment;

import com.google.common.base.Preconditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorServiceLifecycle;
import top.zenyoung.segment.config.SegmentProperties;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * 分段编码-自动配置
 *
 * @author young
 */
@Configuration
@EnableConfigurationProperties({SegmentProperties.class})
public class SegmentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PrefetchWorkerExecutorService prefetchWorkerExecutorService(@Nonnull final SegmentProperties prop) {
        final SegmentProperties.PrefetchWorker prefetchWorker = prop.getChain().getPrefetchWorker();
        Preconditions.checkNotNull(prefetchWorker, "segment.chain.prefetch-worker can not be null!");
        return new PrefetchWorkerExecutorService(prefetchWorker.getPrefetchPeriod(),
                prefetchWorker.getCorePoolSize(), prefetchWorker.isShutdownHook());
    }

    @Bean
    @ConditionalOnMissingBean
    public PrefetchWorkerExecutorServiceLifecycle prefetchWorkerExecutorServiceLifecycle(@Nonnull final PrefetchWorkerExecutorService pwes) {
        return new PrefetchWorkerExecutorServiceLifecycle(pwes);
    }

    @Bean
    @ConditionalOnMissingBean
    public SegmentIdFactory segmentIdFactory(@Nonnull final DataSource dataSource, @Nonnull final PrefetchWorkerExecutorService pwes) {
        return new JdbcSegmentIdFactory(dataSource, pwes);
    }
}
