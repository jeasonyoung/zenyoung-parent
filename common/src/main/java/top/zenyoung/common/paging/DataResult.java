package top.zenyoung.common.paging;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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

    /**
     * 创建数据结果
     *
     * @param data 分页数据结果
     * @param <T>  数据类型
     * @return 数据结果
     */
    public static <T> DataResult<T> of(@Nullable final PagingResult<T> data) {
        if (Objects.nonNull(data)) {
            final List<T> items = data.getRows();
            return of(data.getTotal(), Objects.nonNull(items) ? items : Lists.newArrayList());
        }
        return empty();
    }

    /**
     * 创建数据结果
     *
     * @param items 数据集合
     * @param <T>   数据类型
     * @return 数据结果
     */
    public static <T> DataResult<T> of(@Nullable final List<T> items) {
        if (Objects.nonNull(items)) {
            return of((long) items.size(), items);
        }
        return empty();
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
