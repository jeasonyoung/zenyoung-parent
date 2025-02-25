package top.zenyoung.boot.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Map工具类
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class MapUtils {

    /**
     * 获取Map键值
     *
     * @param map    Map对象
     * @param key    Map键
     * @param valCls 值类型Class
     * @param <V>    值类型
     * @param <T>    键类型
     * @return Map值
     */
    public <V, T> T getVal(@Nonnull final Map<String, V> map, @Nonnull final String key, @Nonnull final Class<T> valCls) {
        return getVal(map, key, valCls::cast);
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
    public <V, T> T getVal(@Nonnull final Map<String, V> map, @Nonnull final String key, @Nonnull final Function<V, T> transform) {
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
    public <T> Map<String, Object> from(@Nonnull final T obj) {
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

    /**
     * 将Map数据赋值生成对象
     *
     * @param map         数据集合
     * @param targetClass 目标类型
     * @param <R>         目标类型
     * @return 目标对象
     */
    public <R> R to(@Nonnull final Map<String, Object> map, @Nonnull final Class<R> targetClass) {
        try {
            if (CollectionUtils.isEmpty(map)) {
                return null;
            }
            final R ret = targetClass.getDeclaredConstructor().newInstance();
            map.forEach((key, val) -> {
                if (!Strings.isNullOrEmpty(key) && Objects.nonNull(val)) {
                    final Field field = ReflectionUtils.findField(targetClass, key, val.getClass());
                    if (Objects.nonNull(field)) {
                        field.setAccessible(true);
                        ReflectionUtils.setField(field, ret, val);
                    }
                }
            });
            return ret;
        } catch (Exception e) {
            log.error("to(map: {},targetClass: {})-exp: {}", map, targetClass, e.getMessage());
            return null;
        }
    }
}
