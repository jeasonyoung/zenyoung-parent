package top.zenyoung.common.util;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import java.util.Map;
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
}