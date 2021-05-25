package top.zenyoung.common.util;

import lombok.extern.slf4j.Slf4j;
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
public class AsyncUtils {

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
                if (bizHandler != null) {
                    bizHandler.run();
                }
            } catch (Throwable ex) {
                log.warn("asyncHandler(executor: {},latch: {},bizHandler: {})-exp: {}", executor, latch, bizHandler, ex.getMessage());
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
