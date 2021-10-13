package top.zenyoung.common.util;

import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 异步工具类
 *
 * @author young
 */
@Slf4j
public class AsyncUtils implements AutoCloseable {
    private final Executor executors;
    private final CountDownLatch latch;

    /**
     * 构造函数
     *
     * @param executors 线程池
     * @param totals    执行总数
     */
    private AsyncUtils(@Nonnull final Executor executors, @Nonnull final Integer totals) {
        if (totals <= 0) {
            throw new IllegalArgumentException("'totals'必须大于0!");
        }
        this.executors = executors;
        this.latch = new CountDownLatch(totals);
    }

    /**
     * 获取实例对象
     *
     * @param executors 线程池
     * @param totals    执行总数
     * @return 异步工具实例
     */
    public static AsyncUtils getInstance(@Nonnull final Executor executors, @Nonnull final Integer totals) {
        return new AsyncUtils(executors, totals);
    }

    /**
     * 异步执行处理
     *
     * @param handler 执行处理器
     * @return 异步处理
     */
    public AsyncUtils asyncHandler(@Nullable final Runnable handler) {
        asyncHandler(executors, latch, handler);
        return this;
    }

    /**
     * 异步执行处理(带数据缓存功能)
     *
     * @param cache    缓存器
     * @param key      缓存键
     * @param supplier 数据生产处理
     * @param consumer 数据消费处理
     * @param <K>      缓存键类型
     * @param <T>      数据类型
     * @return 异步工具实例
     */
    public <K, T> AsyncUtils asyncCacheHandler(@Nonnull final Cache<K, T> cache, @Nonnull final K key, @Nonnull final Supplier<T> supplier, @Nonnull final Consumer<T> consumer) {
        return asyncHandler(() -> {
            //读取缓存
            T data = cache.getIfPresent(key);
            if (data != null) {
                //消费处理
                consumer.accept(data);
                return;
            }
            //获取数据
            data = supplier.get();
            if (data != null) {
                //添加到缓存
                cache.put(key, data);
                //消费处理
                consumer.accept(data);
            }
        });
    }

    /**
     * 同步等待
     */
    public void sync() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.warn("sync()-exp: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        sync();
    }

    /**
     * 异步处理器
     *
     * @param executor   线程池
     * @param latch      线程基数器
     * @param bizHandler 业务处理器
     */
    public static void asyncHandler(@Nonnull final Executor executor, @Nonnull final CountDownLatch latch, @Nullable final Runnable bizHandler) {
        executor.execute(() -> {
            try {
                //业务处理
                if (bizHandler != null) {
                    bizHandler.run();
                }
            } catch (Throwable ex) {
                log.warn("asyncHandler(bizHandler: {})-exp: {}", bizHandler, ex.getMessage());
            } finally {
                latch.countDown();
            }
        });
    }

    /**
     * 异步处理器
     *
     * @param executor    线程池
     * @param bizHandlers 业务处理器
     */
    public static void asyncHandlers(@Nonnull final Executor executor, @Nonnull final List<Runnable> bizHandlers) {
        if (!bizHandlers.isEmpty()) {
            final CountDownLatch latch = new CountDownLatch(bizHandlers.size());
            try {
                //多线程并发处理
                bizHandlers.forEach(handler -> asyncHandler(executor, latch, handler));
                //等待所有的线程执行完成
                latch.await();
            } catch (Throwable ex) {
                log.warn("asyncHandlers(executor: {},bizHandlers: {})-exp: {}", executor, bizHandlers, ex.getMessage());
            }
        }
    }
}
