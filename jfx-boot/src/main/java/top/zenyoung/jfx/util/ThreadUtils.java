package top.zenyoung.jfx.util;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程工具
 *
 * @author young
 */
@Slf4j
public class ThreadUtils {
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private static final AtomicLong REF_COUNT = new AtomicLong(0L);
    private static final long KEEP_ALIVE_TIME = 300L;
    private static final int THREAD_MIN = (CPUS <= 0 ? 5 : CPUS);
    private static final int THREAD_MAX = THREAD_MIN * 2;

    private static ThreadFactory createThreadFactory(@Nonnull final String name) {
        return new ThreadFactoryBuilder().setNameFormat(ThreadUtils.class.getSimpleName() + "_" + REF_COUNT.getAndIncrement() + "_" + name).build();
    }

    public static Executor createPools(@Nullable final Integer min, @Nullable final Integer max) {
        final int corePoolSize = Math.max(min == null ? 0 : min, THREAD_MIN);
        final int maximumPoolSize = Math.max(max == null ? 0 : max, THREAD_MAX);
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(20),
                createThreadFactory(String.format("createPools-%d_%d", corePoolSize, maximumPoolSize)),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    public static Executor createPools() {
        return createPools(null, null);
    }

    public static ScheduledExecutorService createScheduledPools(@Nonnull final Integer pools) {
        Preconditions.checkArgument(pools > 0, "'pools' > 0");
        return new ScheduledThreadPoolExecutor(pools,
                createThreadFactory(String.format("createScheduledPools-%d", pools)),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    public static void mainRun(@Nonnull final Runnable handler) {
        //检查是否为UI线程
        if (Platform.isFxApplicationThread()) {
            handler.run();
            return;
        }
        //UI线程处理
        Platform.runLater(handler);
    }

    public static void threadSleep(@Nonnull final Duration duration) {
        final long waitMillis = duration.toMillis();
        if (waitMillis > 0) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException ex) {
                log.warn("threadSleep(wait: {})-exp: {}", waitMillis, ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void threadSleep(final long millis) {
        if (millis > 0) {
            threadSleep(Duration.ofMillis(millis));
        }
    }

    public static void threadRandomSleep(final long min) {
        final long random = (long) (Math.random() * 1000);
        threadSleep(min > 0 ? Math.min(random, min) : random);
    }

    public static void threadRandomSleep() {
        threadRandomSleep(-1);
    }
}
