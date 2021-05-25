package top.zenyoung.common.util;

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
            bizHandlers.forEach(handler -> asyncHandler(executor, latch, handler));
        }
    }
}
