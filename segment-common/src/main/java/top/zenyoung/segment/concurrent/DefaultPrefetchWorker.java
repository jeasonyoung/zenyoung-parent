package top.zenyoung.segment.concurrent;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 默认预取器
 *
 * @author young
 */
@Slf4j
public class DefaultPrefetchWorker extends Thread implements PrefetchWorker {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    private volatile boolean shutdown = false;
    private final Duration prefetchPeriod;

    private final CopyOnWriteArraySet<AffinityJob> affinityJobs = Sets.newCopyOnWriteArraySet();

    public DefaultPrefetchWorker(@Nonnull final Duration prefetchPeriod) {
        super(Strings.lenientFormat("DefaultPrefetchWorker-" + THREAD_COUNTER.incrementAndGet()));
        this.prefetchPeriod = prefetchPeriod;
    }

    @Override
    public void submit(@Nonnull final AffinityJob job) {
        log.info("submit - [{}] jobSize: [{}].", job.getJobId(), affinityJobs.size());
        if (shutdown) {
            throw new IllegalArgumentException("PrefetchWorker is shutdown.");
        }
        affinityJobs.add(job);
    }

    @Override
    public void wakeup(@Nonnull final AffinityJob job) {
        log.debug("wakeup - [{}] state: [{}]", job.getJobId(), getState());
        if (shutdown) {
            log.warn("wakeup - [{}] - PrefetchWorker is shutdown,Can't be awakened.", job.getJobId());
            return;
        }
        if (State.RUNNABLE.equals(getState())) {
            log.debug("wakeup - [{}] - PrefetchWorker is running,Don't need to be awakened.", job.getJobId());
            return;
        }
        LockSupport.unpark(this);
    }

    @Override
    public void run() {
        while (!shutdown) {
            try {
                affinityJobs.forEach(job -> {
                    try {
                        job.run();
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                });
                LockSupport.parkNanos(this, prefetchPeriod.toNanos());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void shutdown() {
        if (log.isInfoEnabled()) {
            log.info("shutdown.");
        }
        if (shutdown) {
            return;
        }
        shutdown = true;
    }
}
