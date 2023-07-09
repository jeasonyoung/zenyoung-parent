package top.zenyoung.common.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;

/**
 * Java SPI工具类
 *
 * @author young
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SpiUtils {

    /**
     * 加载类型实例
     *
     * @param cls       类型
     * @param predicate 判断是否可用
     * @param <T>       类型
     * @return 类型实例
     */
    public static <T> T load(@Nonnull final Class<T> cls, @Nullable final Predicate<T> predicate) {
        final ServiceLoader<T> services = ServiceLoader.load(cls);
        for (final T service : services) {
            if (Objects.nonNull(service)) {
                if (Objects.isNull(predicate)) {
                    return service;
                }
                if (predicate.test(service)) {
                    return service;
                }
            }
        }
        return null;
    }

    /**
     * 加载类型实例
     *
     * @param cls 类型
     * @param <T> 类型
     * @return 类型实例
     */
    public static <T> T load(@Nonnull final Class<T> cls) {
        return load(cls, t -> true);
    }
}
