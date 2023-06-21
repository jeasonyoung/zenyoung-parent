package top.zenyoung.common.paging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * 分页数据-查询结果
 *
 * @author yangyong
 * @version 1.0
 **/
public interface PagingResult<T> extends PageList<T> {

    /**
     * 构建分页数据
     *
     * @param pageList 分页数据
     * @param convert  数据转换
     * @param <T>      转换前数据类型
     * @param <R>      转换后数据类型
     * @return 构建结果
     */
    static <T, R> PagingResult<R> of(@Nullable final PagingResult<T> pageList, @Nonnull final Function<T, R> convert) {
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
    static <T, R> PagingResult<R> ofHandler(@Nullable final PagingResult<T> pageList, @Nonnull final Function<List<T>, List<R>> handler) {
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
    static <T, R> PagingResult<R> of(@Nullable final List<T> rows, @Nonnull final Function<T, R> convert) {
        return DataResult.of(rows, convert);
    }

    /**
     * 创建数据结果
     *
     * @param pageList 分页数据
     * @param <T>      数据类型
     * @return 数据结果
     */
    static <T> PagingResult<T> of(@Nullable final PagingResult<T> pageList) {
        return DataResult.of(pageList);
    }

    /**
     * 创建数据结果
     *
     * @param items 数据集合
     * @param <T>   数据类型
     * @return 数据结果
     */
    static <T> PagingResult<T> of(@Nullable final List<T> items) {
        return DataResult.of(items);
    }

    /**
     * 空对象方法
     *
     * @param <R> 类型
     * @return 空对象
     */
    static <R> PagingResult<R> empty() {
        return DataResult.empty();
    }
}