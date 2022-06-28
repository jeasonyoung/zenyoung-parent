package top.zenyoung.common.util;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Java SPI工具类
 * @author young
 */
public class SpiUtils {

    /**
     * 加载类型实例
     *
     * @param loadClass 类型
     * @param <T>       类型
     * @return 类型实例
     */
    public static <T> T load(@Nonnull final Class<T> loadClass) {
        final ServiceLoader<T> serviceLoaders = ServiceLoader.load(loadClass);
        for (T t : serviceLoaders) {
            if (Objects.nonNull(t)) {
                return t;
            }
        }
        return null;
    }
}
