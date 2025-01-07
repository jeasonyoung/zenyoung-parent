package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 计算工具类
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class CalcUtils {

    public static Executor newCachedExecutor(final int maxSize) {
        final int cpus = Math.max(Runtime.getRuntime().availableProcessors(), 2) / 2;
        final int core = Math.max(Math.min(cpus, maxSize), 2);
        log.info("calc-pool_new: maxSize={}, cpus/2: {} => {}", maxSize, cpus, core);
        final ThreadFactory factory = new ThreadFactoryBuilder()
                .setNameFormat("calc-pool[" + core + "]-%d")
                .setDaemon(true)
                .build();
        return new ThreadPoolExecutor(
                core,
                core + 1,
                20L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                factory
        );
    }

    public static <T, R> List<R> split(@Nullable final Collection<T> items, @Nonnull final Function<T, R> convert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .map(convert)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public static <T, R> Set<R> set(@Nullable final Collection<T> items, @Nonnull final Function<T, R> convert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .map(convert)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }

    public static <T, K, V> Map<K, V> map(@Nullable final Collection<T> items,
                                          @Nonnull final Function<T, K> keyConvert,
                                          @Nonnull final Function<T, V> valConvert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .filter(t -> Objects.nonNull(keyConvert.apply(t)) && Objects.nonNull(valConvert.apply(t)))
                    .collect(Collectors.toMap(keyConvert, valConvert, (o, n) -> n));
        }
        return Maps.newHashMap();
    }

    public static <T, K, V> Map<K, List<V>> group(@Nullable final Collection<T> items,
                                                  @Nonnull final Function<T, K> keyConvert,
                                                  @Nonnull final Function<T, V> valConvert) {
        if (Objects.nonNull(items) && !items.isEmpty()) {
            return items.stream()
                    .filter(t -> Objects.nonNull(keyConvert.apply(t)) && Objects.nonNull(valConvert.apply(t)))
                    .collect(Collectors.groupingBy(keyConvert, Collectors.mapping(valConvert, Collectors.toList())));
        }
        return Maps.newHashMap();
    }

    public static <K, V> void assign(@Nonnull final Supplier<K> keyHandler,
                                     @Nonnull final Map<K, V> valMap,
                                     @Nonnull final Consumer<V> assignHandler) {
        assign(keyHandler, valMap, assignHandler, null);
    }

    public static <K, V> void assign(@Nonnull final Supplier<K> keyHandler,
                                     @Nonnull final Map<K, V> valMap,
                                     @Nonnull final Consumer<V> assignHandler,
                                     @Nullable final Supplier<? extends V> defaultVal) {
        Optional.ofNullable(keyHandler.get())
                .filter(key -> {
                    if (key instanceof String) {
                        return !Strings.isNullOrEmpty((String) key);
                    }
                    return true;
                })
                .map(key -> {
                    V val = valMap.getOrDefault(key, null);
                    if (val == null && defaultVal != null) {
                        val = defaultVal.get();
                    }
                    return val;
                })
                .ifPresent(assignHandler);
    }

    public static <R> CompletableFuture<R> async(@Nonnull final Supplier<R> valHandler) {
        return CompletableFuture.supplyAsync(() -> {
            final StopWatch watch = new StopWatch();
            try {
                //开始计时
                watch.start("calc-pool_nasync:" + Thread.currentThread().getName());
                //执行业务
                return valHandler.get();
            } finally {
                watch.stop();
                log.info("calc-pool_nasync: {}", watch.shortSummary());
            }
        });

    }

    public static <R> CompletableFuture<Void> async(@Nonnull final Supplier<R> valHandler, @Nonnull final Consumer<R> assignHandler) {
        final StopWatch watch = new StopWatch();
        return CompletableFuture.supplyAsync(() -> {
            try {
                watch.start("calc-pool_nasync_1:" + Thread.currentThread().getName());
                return valHandler.get();
            } finally {
                watch.stop();
            }
        }).thenAcceptAsync(ret -> {
            try {
                watch.start("calc-pool_nasync_2:" + Thread.currentThread().getName());
                assignHandler.accept(ret);
            } finally {
                watch.stop();
                log.info("calc-pool_nasync12: {}", watch.prettyPrint());
            }
        });

    }

    public static <R> CompletableFuture<R> async(@Nonnull final Executor executor, @Nonnull final Supplier<R> valHandler) {
        return CompletableFuture.supplyAsync(() -> {
            final StopWatch watch = new StopWatch();
            try {
                //开始计时
                watch.start("calc-pool_async:" + Thread.currentThread().getName());
                //执行业务
                return valHandler.get();
            } finally {
                watch.stop();
                log.info("calc-pool_async: {}", watch.shortSummary());
            }
        }, executor);

    }

    public static <R> CompletableFuture<Void> async(@Nonnull final Executor executor, @Nonnull final Supplier<R> valHandler, @Nonnull final Consumer<R> assignHandler) {
        final StopWatch watch = new StopWatch();
        return CompletableFuture.supplyAsync(() -> {
            try {
                watch.start("calc-pool_async_1:" + Thread.currentThread().getName());
                return valHandler.get();
            } finally {
                watch.stop();
            }
        }, executor).thenAcceptAsync(ret -> {
            try {
                watch.start("calc-pool_async_2:" + Thread.currentThread().getName());
                assignHandler.accept(ret);
            } finally {
                watch.stop();
                log.info("calc-pool_async12: {}", watch.prettyPrint());
            }
        }, executor);
    }

    public static void syncJoin(@Nonnull final List<CompletableFuture<?>> futures) {
        //检查是否存在
        if (futures.isEmpty()) {
            return;
        }
        final int totals = futures.size();
        final long start = System.currentTimeMillis();
        log.info("start_calc-pool:{}[start: {}]", totals, start);
        //同步等待
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((res, ex) -> {
                    if (Objects.nonNull(ex)) {
                        final String err = ex.getMessage();
                        if (!Strings.isNullOrEmpty(err)) {
                            log.warn(err);
                        }
                    }
                })
                .join();
        //计算耗时
        final long duration = System.currentTimeMillis() - start;
        log.info("end_calc-pool:{}[duration: {}ms]", totals, duration);
    }
}
