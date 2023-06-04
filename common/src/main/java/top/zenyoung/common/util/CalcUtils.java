package top.zenyoung.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 计算工具类
 *
 * @author young
 */
public class CalcUtils {

    public static <T, R> List<R> split(@Nullable final List<T> items, @Nonnull final Function<T, R> convert) {
        if (items != null && items.size() > 0) {
            return items.stream()
                    .map(convert)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public static <T, K, V> Map<K, V> map(@Nullable final List<T> items, @Nonnull final Function<T, K> keyConvert,
                                          @Nonnull final Function<T, V> valConvert) {
        if (items != null && items.size() > 0) {
            return items.stream()
                    .filter(t -> Objects.nonNull(keyConvert.apply(t)) && Objects.nonNull(valConvert.apply(t)))
                    .collect(Collectors.toMap(keyConvert, valConvert, (o, n) -> n));
        }
        return Maps.newHashMap();
    }

    public static <T, K, V> Map<K, List<V>> group(@Nullable final List<T> items, @Nonnull final Function<T, K> keyConvert,
                                                  @Nonnull final Function<T, V> valConvert) {
        if (items != null && items.size() > 0) {
            return items.stream()
                    .filter(t -> Objects.nonNull(keyConvert.apply(t)) && Objects.nonNull(valConvert.apply(t)))
                    .collect(Collectors.groupingBy(keyConvert, Collectors.mapping(valConvert, Collectors.toList())));
        }
        return Maps.newHashMap();
    }
}
