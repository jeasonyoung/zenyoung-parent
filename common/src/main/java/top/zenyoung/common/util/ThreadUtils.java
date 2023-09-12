package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.experimental.UtilityClass;
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
@UtilityClass
public class ThreadUtils {
    private static final AtomicLong REF_COUNT = new AtomicLong(0L);
    private static final Duration KEEP_ALIVE = Duration.ofSeconds(300);
    private static final int MIN = Math.max(Runtime.getRuntime().availableProcessors(), 2);
    private static final int MAX = MIN * 2;

    /**
     * 创建线程工厂
     *
     * @param daemon 是否为后台线程
     * @param prefix 线程名称前缀
     * @return 线程工厂
     */
    public static ThreadFactory createThreadFactory(final boolean daemon, @Nullable final String prefix) {
        log.debug("createThreadFactory(daemon: {},prefix: {})...", daemon, prefix);
        final long refIdx = REF_COUNT.incrementAndGet();
        final String nameFormat = (Strings.isNullOrEmpty(prefix) ? "" : prefix) + "[" + refIdx + "]-pools-%d";
        return new ThreadFactoryBuilder()
                .setDaemon(daemon)
                .setNameFormat(nameFormat)
                .build();
    }

    /**
     * 创建线程工厂
     *
     * @param prefix 工厂名称
     * @return 线程工厂
     */
    public static ThreadFactory createThreadFactory(@Nullable final String prefix) {
        return createThreadFactory(true, prefix);
    }

    private static BlockingQueue<Runnable> createBlockingQueue() {
        return new LinkedBlockingDeque<>(100);
    }

    private static RejectedExecutionHandler createRejectedHandler() {
        return new ThreadPoolExecutor.DiscardOldestPolicy();
    }

    public static Executor createPools(final boolean daemon, @Nullable final String prefix,
                                       @Nullable final Integer min, @Nullable final Integer max) {
        log.debug("createPools(daemon: {},prefix: {},min: {},max: {})...", daemon, prefix, min, max);
        final int corePoolSize = Math.max(min == null ? 0 : min, MIN);
        final int maxPoolSize = Math.max(max == null ? 0 : max, MAX);
        final long keepAliveTimeSeconds = KEEP_ALIVE.getSeconds();
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAliveTimeSeconds, TimeUnit.SECONDS,
                createBlockingQueue(), createThreadFactory(daemon, prefix), createRejectedHandler());
    }

    /**
     * 创建线程池
     *
     * @param min 核心线程数
     * @param max 最大线程数
     * @return 线程池
     */
    public static Executor createPools(@Nullable final Integer min, @Nullable final Integer max) {
        return createPools(true, "default", min, max);
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
                createThreadFactory("default-scheduled"), createRejectedHandler());
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
        } catch (InterruptedException e) {
            log.warn("sleep(wait: {})-exp: {}", waitMillis, e.getMessage());
            Thread.currentThread().interrupt();
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
        final long random = RandomUtils.getRandom().nextLong() * 100;
        sleep(Math.max(minMillis, random));
    }

    /**
     * 线程随机等待
     */
    public static void randomSleep() {
        randomSleep(-1);
    }
}
