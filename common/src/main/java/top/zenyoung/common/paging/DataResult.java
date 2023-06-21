package top.zenyoung.common.paging;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据结果
 *
 * @author yangyong
 * @version 1.0
 **/
@Getter
@RequiredArgsConstructor(staticName = "of")
public class DataResult<T> implements PagingResult<T> {
    /**
     * 数据总数
     */
    private final Long total;
    /**
     * 数据集合
     */
    private final List<T> rows;

    private static <T, R> List<R> convertHandler(@Nullable final List<T> rows, @Nonnull final Function<T, R> convert) {
        return Optional.ofNullable(rows)
                .filter(items -> items.size() > 0)
                .map(items -> items.stream()
                        .filter(Objects::nonNull)
                        .map(convert)
                        .collect(Collectors.toList())
                )
                .orElse(Lists.newArrayList());
    }

    /**
     * 构建分页数据
     *
     * @param pageList 分页数据
     * @param convert  数据转换
     * @param <T>      转换前数据类型
     * @param <R>      转换后数据类型
     * @return 构建结果
     */
    public static <T, R> DataResult<R> of(@Nullable final PageList<T> pageList, @Nonnull final Function<T, R> convert) {
        if (Objects.nonNull(pageList)) {
            return of(pageList.getTotal(), convertHandler(pageList.getRows(), convert));
        }
        return empty();
    }

    /**
     * 构建分页数据处理
     *
     * @param pageList 分页数据
     * @param handler  数据处理
     * @param <T>      转换前数据类型
     * @param <R>      转换后数据类型
     * @return 构建结果
     */
    public static <T, R> DataResult<R> ofHandler(@Nullable final PageList<T> pageList, @Nonnull final Function<List<T>, List<R>> handler) {
        if (Objects.nonNull(pageList)) {
            return of(pageList.getTotal(), handler.apply(pageList.getRows()));
        }
        return empty();
    }

    /**
     * 构建分页数据
     *
     * @param rows    数据集合
     * @param convert 数据转换
     * @param <T>     转换前数据类型
     * @param <R>     转换后数据类型
     * @return 构建结果
     */
    public static <T, R> DataResult<R> of(@Nullable final List<T> rows, @Nonnull final Function<T, R> convert) {
        if (Objects.nonNull(rows) && rows.size() > 0) {
            final List<R> items = convertHandler(rows, convert);
            return of((long) items.size(), items);
        }
        return empty();
    }

    /**
     * 创建数据结果
     *
     * @param pageList 分页数据
     * @param <T>      数据类型
     * @return 数据结果
     */
    public static <T> DataResult<T> of(@Nullable final PageList<T> pageList) {
        return of(pageList, Function.identity());
    }

    /**
     * 创建数据结果
     *
     * @param items 数据集合
     * @param <T>   数据类型
     * @return 数据结果
     */
    public static <T> DataResult<T> of(@Nullable final List<T> items) {
        return of(items, Function.identity());
    }

    /**
     * 创建空数据结果
     *
     * @param <T> 数据类型
     * @return 空数据结果
     */
    public static <T> DataResult<T> empty() {
        return of(0L, Lists.newArrayList());
    }
}
