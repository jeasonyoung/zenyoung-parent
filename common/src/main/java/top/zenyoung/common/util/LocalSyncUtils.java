package top.zenyoung.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 本地同步工具类
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalSyncUtils {
    /**
     * 同步处理
     *
     * @param locks   同步锁
     * @param key     锁键
     * @param handler 同步业务处理
     * @param <T>     返回数据类型
     * @return 返回业务数据
     */
    public static <T> T syncHandler(@Nonnull final Map<String, Object> locks, @Nonnull final String key,
                                    @Nonnull final Supplier<T> handler) {
        synchronized (locks.computeIfAbsent(key, k -> new Object())) {
            try {
                return handler.get();
            } finally {
                locks.remove(key);
            }
        }
    }

    /**
     * 同步处理
     *
     * @param locks   同步锁
     * @param key     锁键
     * @param handler 同步业务处理
     */
    public static void syncHandler(@Nonnull final Map<String, Object> locks, @Nonnull final String key,
                                   @Nonnull final Runnable handler) {
        synchronized (locks.computeIfAbsent(key, k -> new Object())) {
            try {
                handler.run();
            } finally {
                locks.remove(key);
            }
        }
    }
}
