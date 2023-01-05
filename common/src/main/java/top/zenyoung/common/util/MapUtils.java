package top.zenyoung.common.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.Function;

/**
 * Map工具类
 *
 * @author young
 */
public class MapUtils {

    /**
     * 获取Map键值
     *
     * @param map      Map对象
     * @param key      Map键
     * @param valClass 值类型Class
     * @param <V>      值类型
     * @param <T>      键类型
     * @return Map值
     */
    public static <V, T> T getVal(@Nonnull final Map<String, V> map, @Nonnull final String key, @Nonnull final Class<T> valClass) {
        return getVal(map, key, valClass::cast);
    }

    /**
     * 获取Map键值
     *
     * @param map       Map对象
     * @param key       Map键
     * @param transform Map值转换
     * @param <V>       值类型
     * @param <T>       键类型
     * @return Map值
     */
    public static <V, T> T getVal(@Nonnull final Map<String, V> map, @Nonnull final String key, @Nonnull final Function<V, T> transform) {
        if (!map.isEmpty() && !Strings.isNullOrEmpty(key)) {
            final V val = map.get(key);
            if (val != null) {
                return transform.apply(val);
            }
        }
        return null;
    }

    /**
     * 将对象转换为Map集合
     *
     * @param obj 对象
     * @param <T> 对象类型
     * @return Map集合
     */
    public static <T> Map<String, Object> from(@Nonnull final T obj) {
        final Map<String, Object> map = Maps.newHashMap();
        ReflectionUtils.doWithFields(obj.getClass(), f -> {
            f.setAccessible(true);
            final Object val = f.get(obj);
            if (Objects.nonNull(val)) {
                map.put(f.getName(), val);
            }
        });
        return map;
    }

    @SneakyThrows({})
    @SuppressWarnings({"unchecked"})
    public static <T, R> R to(@Nonnull final Map<String, T> map, @Nonnull final Class<R> cls) {
        final R ret = cls.newInstance();
        if (!CollectionUtils.isEmpty(map)) {
            final List<Field> fields = Lists.newArrayList();
            ReflectionUtils.doWithFields(cls, field -> {
                field.setAccessible(true);
                fields.add(field);
            });
            if (!CollectionUtils.isEmpty(fields)) {
                fields.parallelStream()
                        .filter(Objects::nonNull)
                        .forEach(field -> {
                            final String name = field.getName();
                            final T val = map.getOrDefault(name, null);
                            if (Objects.nonNull(val)) {
                                if (val instanceof Map) {
                                    final Object sub = to((Map<String, ?>) val, (Class<?>) field.getClass());
                                    ReflectionUtils.setField(field, ret, sub);
                                } else {
                                    ReflectionUtils.setField(field, ret, val);
                                }
                            }
                        });
            }
        }
        return ret;
    }
}
