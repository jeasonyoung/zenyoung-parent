package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程工具类
 *
 * @author young
 */
@Slf4j
public class ThreadUtils {
    private static final AtomicLong REF_COUNT = new AtomicLong(0L);
    private static final Duration KEEP_ALIVE = Duration.ofSeconds(300);
    private static final int MIN = Math.max(Runtime.getRuntime().availableProcessors(), 2);
    private static final int MAX = MIN * 2;

    /**
     * 创建线程工厂
     *
     * @param name 工厂名称
     * @return 线程工厂
     */
    public static ThreadFactory createThreadFactory(@Nullable final String name) {
        log.debug("createThreadFactory(name: {})...", name);
        final String nameFormat = ThreadUtils.class.getSimpleName() + "_" + REF_COUNT.getAndIncrement() + (Strings.isNullOrEmpty(name) ? "" : "_" + name) + "-pools-%d";
        return new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
    }

    private static BlockingQueue<Runnable> createBlockingQueue() {
        return new LinkedBlockingDeque<>(100);
    }

    private static RejectedExecutionHandler createRejectedHandler() {
        return new ThreadPoolExecutor.DiscardOldestPolicy();
    }

    /**
     * 创建线程池
     *
     * @param min 核心线程数
     * @param max 最大线程数
     * @return 线程池
     */
    public static Executor createPools(@Nullable final Integer min, @Nullable final Integer max) {
        log.debug("createPools(min: {},max: {})...", min, max);
        final int corePoolSize = Math.max(min == null ? 0 : min, MIN);
        final int maxPoolSize = Math.max(max == null ? 0 : max, MAX);
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, KEEP_ALIVE.getSeconds(), TimeUnit.SECONDS,
                createBlockingQueue(), createThreadFactory("createPools"), createRejectedHandler());
    }

    /**
     * 创建线程池
     *
     * @return 线程池
     */
    public static Executor createPools() {
        return createPools(null, null);
    }

    /**
     * 创建定时线程池
     *
     * @param pools 线程数
     * @return 线程池
     */
    public static ScheduledExecutorService createScheduledPools(@Nullable final Integer pools) {
        log.debug("createScheduledPools(pools: {})...", pools);
        return new ScheduledThreadPoolExecutor(Math.max(pools == null ? 0 : pools, 1),
                createThreadFactory("createScheduledPools"), createRejectedHandler());
    }

    /**
     * 线程等待
     *
     * @param duration 等待时长
     */
    public static void sleep(@Nonnull final Duration duration) {
        final long waitMillis = duration.toMillis();
        try {
            if (waitMillis > 0) {
                Thread.sleep(waitMillis);
            }
        } catch (Throwable ex) {
            log.warn("sleep(wait: {})-exp: {}", waitMillis, ex.getMessage());
        }
    }

    /**
     * 线程等待
     *
     * @param millis 等待时长(毫秒)
     */
    public static void sleep(final long millis) {
        if (millis > 0) {
            sleep(Duration.ofMillis(millis));
        }
    }

    /**
     * 线程随机等待
     *
     * @param minMillis 最小等待时长
     */
    public static void randomSleep(final long minMillis) {
        final long random = (long) (Math.random() * 100);
        sleep(Math.max(minMillis, random));
    }

    /**
     * 线程随机等待
     */
    public static void randomSleep() {
        randomSleep(-1);
    }
}
