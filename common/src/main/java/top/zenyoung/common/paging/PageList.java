package top.zenyoung.common.paging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 分页数据列表
 *
 * @author young
 */
public interface PageList<T> extends Serializable {
    /**
     * 获取数据总数
     *
     * @return 数据总数
     */
    Long getTotal();

    /**
     * 获取查询数据结果集合
     *
     * @return 数据结果集合
     */
    Collection<T> getRows();

    /**
     * 构建分页数据
     *
     * @param pageList 分页数据
     * @param convert  数据转换
     * @param <T>      转换前数据类型
     * @param <R>      转换后数据类型
     * @return 构建结果
     */
    static <T, R> PageList<R> of(@Nullable final PageList<T> pageList, @Nonnull final Function<T, R> convert) {
        return DataResult.of(pageList, convert);
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
    static <T, R> PageList<R> ofHandler(@Nullable final PageList<T> pageList, @Nonnull final Function<Collection<T>, Collection<R>> handler) {
        return DataResult.ofHandler(pageList, handler);
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
    static <T, R> PageList<R> of(@Nullable final List<T> rows, @Nonnull final Function<T, R> convert) {
        return DataResult.of(rows, convert);
    }

    /**
     * 创建数据结果
     *
     * @param pageList 分页数据
     * @param <T>      数据类型
     * @return 数据结果
     */
    static <T> PageList<T> of(@Nullable final PageList<T> pageList) {
        return DataResult.of(pageList);
    }

    /**
     * 创建数据结果
     *
     * @param items 数据集合
     * @param <T>   数据类型
     * @return 数据结果
     */
    static <T> PageList<T> of(@Nullable final List<T> items) {
        return DataResult.of(items);
    }

    /**
     * 空对象方法
     *
     * @param <T> 类型
     * @return 空对象
     */
    static <T> PageList<T> empty() {
        return DataResult.empty();
    }

    /**
     * 创建数据结果
     *
     * @param total 总数量
     * @param rows  数据集合
     * @param <T>   数据类型
     * @return 数据结果
     */
    static <T> PageList<T> of(@Nullable final Long total, @Nullable final List<T> rows) {
        return DataResult.of(total, rows);
    }
}
