package top.zenyoung.segment;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private SegmentProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public PrefetchWorkerExecutorService prefetchWorkerExecutorService() {
        final SegmentProperties.PrefetchWorker prefetchWorker = properties.getChain().getPrefetchWorker();
        Preconditions.checkNotNull(prefetchWorker, "segment.chain.prefetch-worker can not be null!");
        return new PrefetchWorkerExecutorService(prefetchWorker.getPrefetchPeriod(),
                prefetchWorker.getCorePoolSize(), prefetchWorker.isShutdownHook());
    }

    @Bean
    @ConditionalOnMissingBean
    public PrefetchWorkerExecutorServiceLifecycle prefetchWorkerExecutorServiceLifecycle(final PrefetchWorkerExecutorService prefetchWorkerExecutorService) {
        return new PrefetchWorkerExecutorServiceLifecycle(prefetchWorkerExecutorService);
    }

    @Bean
    @ConditionalOnMissingBean
    public SegmentIdFactory segmentIdFactory(@Nonnull final DataSource dataSource, @Nonnull final PrefetchWorkerExecutorService prefetchWorkerExecutorService) {
        return new JdbcSegmentIdFactory(dataSource, prefetchWorkerExecutorService);
    }
}
