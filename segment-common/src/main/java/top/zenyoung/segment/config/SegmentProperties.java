package top.zenyoung.segment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.zenyoung.segment.concurrent.PrefetchWorkerExecutorService;

import java.time.Duration;

/**
 * 分段编码配置
 *
 * @author young
 */
@Data
@ConfigurationProperties(prefix = "top.zenyoung.segment")
public class SegmentProperties {
    private Chain chain = new Chain();

    @Data
    public static class Chain {
        private PrefetchWorker prefetchWorker;

        public Chain() {
            this.prefetchWorker = new PrefetchWorker();
        }
    }

    @Data
    public static class PrefetchWorker {
        private Duration prefetchPeriod = PrefetchWorkerExecutorService.DEFAULT_PREFETCH_PERIOD;
        private int corePoolSize = Runtime.getRuntime().availableProcessors();
        private boolean shutdownHook = true;
    }
}
