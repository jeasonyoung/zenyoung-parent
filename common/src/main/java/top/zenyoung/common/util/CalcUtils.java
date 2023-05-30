package top.zenyoung.common.util;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
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
}
