package top.zenyoung.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

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
    public AsyncUtils(@Nonnull final Executor executors, @Nonnull final Integer totals) {
        Assert.isTrue(totals > 0, "'totals'必须大于0!");
        this.executors = executors;
        this.latch = new CountDownLatch(totals);
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
        if (!CollectionUtils.isEmpty(bizHandlers)) {
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
