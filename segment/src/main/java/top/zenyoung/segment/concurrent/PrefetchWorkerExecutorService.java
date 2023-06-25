package top.zenyoung.segment.concurrent;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 预取器线程池管理
 *
 * @author young
 */
@Slf4j
public class PrefetchWorkerExecutorService {
    public static final Duration DEFAULT_PREFETCH_PERIOD = Duration.ofSeconds(1);

    public static final PrefetchWorkerExecutorService DEFAULT;

    static {
        final int cpus = Runtime.getRuntime().availableProcessors();
        DEFAULT = new PrefetchWorkerExecutorService(DEFAULT_PREFETCH_PERIOD, cpus);
    }

    private volatile boolean shutdown = false;
    private final int corePoolSize;
    private final Duration prefetchPeriod;
    private final PrefetchWorker[] workers;
    private boolean initialized = false;
    private final AtomicLong threadIdx = new AtomicLong(0L);

    public PrefetchWorkerExecutorService(final Duration prefetchPeriod, final int corePoolSize) {
        this(prefetchPeriod, corePoolSize, true);
    }

    public PrefetchWorkerExecutorService(final Duration prefetchPeriod, final int corePoolSize, final boolean shutdownHook) {
        Preconditions.checkArgument(corePoolSize > 0, "corePoolSize:[%s] must be greater than 0.", corePoolSize);
        this.prefetchPeriod = prefetchPeriod;
        this.corePoolSize = corePoolSize;
        this.workers = new PrefetchWorker[corePoolSize];
        if (shutdownHook) {
            Runtime.getRuntime().addShutdownHook(new GracefullyCloser(this));
        }
    }

    private void ensureInitWorkers() {
        if (initialized) {
            return;
        }
        initialized = true;
        for (int i = 0; i < corePoolSize; i++) {
            final DefaultPrefetchWorker prefetchWorker = new DefaultPrefetchWorker(prefetchPeriod);
            prefetchWorker.setDaemon(true);
            workers[i] = prefetchWorker;
            log.debug("initWorkers - [{}].", prefetchWorker.getName());
        }
    }

    private PrefetchWorker chooseWorker() {
        return workers[(int) Math.abs(threadIdx.getAndIncrement() % workers.length)];
    }

    public void submit(@Nonnull final AffinityJob job) {
        Preconditions.checkNotNull(job, "job can not be null.");
        log.info("submit - jobId: [{}].", job.getJobId());
        if (shutdown) {
            throw new IllegalStateException("PrefetchWorkerExecutorService is shutdown.");
        }
        if (job.getPrefetchWorker() != null) {
            return;
        }
        synchronized (this) {
            if (job.getPrefetchWorker() != null) {
                return;
            }
            ensureInitWorkers();
            final DefaultPrefetchWorker prefetchWorker = (DefaultPrefetchWorker) chooseWorker();
            log.info("submit - jobId: [{}] is bound to thread: [{}].", job.getJobId(), prefetchWorker.getName());
            if (Thread.State.NEW.equals(prefetchWorker.getState())) {
                log.info("submit - jobId:[{}] is bound to thread:[{}] start.", job.getJobId(), prefetchWorker.getName());
                prefetchWorker.start();
            }
            prefetchWorker.submit(job);
            job.setPrefetchWorker(prefetchWorker);
        }
    }

    public void shutdown() {
        log.info("shutdown.");
        if (shutdown) {
            return;
        }
        shutdown = true;
        for (final PrefetchWorker worker : workers) {
            if (Objects.nonNull(worker)) {
                worker.shutdown();
            }
        }
    }

    private static class GracefullyCloser extends Thread {
        private final PrefetchWorkerExecutorService service;

        public GracefullyCloser(final PrefetchWorkerExecutorService service) {
            this.service = service;
        }

        @Override
        public void run() {
            log.info("Close gracefully!");
            service.shutdown();
        }
    }
}
