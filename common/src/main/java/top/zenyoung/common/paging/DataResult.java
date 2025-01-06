package top.zenyoung.common.paging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor(staticName = "of")
public class DataResult<T> implements PagingResult<T> {
    /**
     * 数据总数
     */
    private final Long total;
    /**
     * 数据集合
     */
    private final Collection<T> rows;

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
            final List<R> rows = Optional.ofNullable(pageList.getRows())
                    .filter(items -> !items.isEmpty())
                    .map(items -> items.stream()
                            .filter(Objects::nonNull)
                            .map(convert)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
                    )
                    .orElseGet(Lists::newArrayList);
            return of(pageList.getTotal(), rows);
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
    public static <T, R> DataResult<R> ofHandler(@Nullable final PageList<T> pageList, @Nonnull final Function<Collection<T>, Collection<R>> handler) {
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
    public static <T, R> DataResult<R> of(@Nullable final Collection<T> rows, @Nonnull final Function<T, R> convert) {
        if (Objects.nonNull(rows) && !rows.isEmpty()) {
            final List<R> items = rows.stream()
                    .filter(Objects::nonNull)
                    .map(convert)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
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
    public static <T> DataResult<T> of(@Nullable final Collection<T> items) {
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
