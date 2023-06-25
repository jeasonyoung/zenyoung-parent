package top.zenyoung.segment.concurrent;

import org.springframework.context.SmartLifecycle;

import javax.annotation.Nonnull;

/**
 * 预取器线程池管理-生命周期
 *
 * @author young
 */
public class PrefetchWorkerExecutorServiceLifecycle implements SmartLifecycle {
    private volatile boolean running;
    private final PrefetchWorkerExecutorService service;

    public PrefetchWorkerExecutorServiceLifecycle(@Nonnull final PrefetchWorkerExecutorService service) {
        this.service = service;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        service.shutdown();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
