package top.zenyoung.framework.utils;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Bean缓存工具类
 *
 * @author young
 */
@Slf4j
public class BeanCacheUtils {
    private static final Map<Class<?>, Object> LOCKS = Maps.newConcurrentMap();
    private static final Map<Class<?>, Object> CACHE = Maps.newConcurrentMap();

    public static <T> T getBean(@Nonnull final ApplicationContext context, @Nonnull final Class<T> tClass) {
        final Object o = CACHE.get(tClass);
        if (Objects.nonNull(o) && o.getClass() == tClass) {
            return tClass.cast(o);
        }
        synchronized (LOCKS.computeIfAbsent(tClass, k -> new Object())) {
            try {
                final T bean = context.getBean(tClass);
                CACHE.put(tClass, bean);
                return bean;
            } catch (BeansException e) {
                log.warn("getBean(context: {},tClass: {})-exp: {}", context, tClass, e.getMessage());
                return null;
            } finally {
                LOCKS.remove(tClass);
            }
        }
    }

    public static <T, R> R function(@Nonnull final ApplicationContext context, @Nonnull final Class<T> tClass, @Nonnull final Function<T, R> handler) {
        final T bean = getBean(context, tClass);
        if (Objects.nonNull(bean)) {
            return handler.apply(bean);
        }
        return null;
    }

    public static <T> void consumer(@Nonnull final ApplicationContext context, @Nonnull final Class<T> tClass, @Nonnull final Consumer<T> handler) {
        function(context, tClass, bean -> {
            handler.accept(bean);
            return null;
        });
    }
}
