package top.zenyoung.common.paging;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.List;

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

    public static <T> DataResult<T> of(@Nullable final PagingResult<T> data) {
        return DataResult.of(data == null ? 0L : data.getTotal(), data == null ? Lists.newLinkedList() : data.getRows());
    }

    public static <T> DataResult<T> of(@Nullable final List<T> items) {
        final boolean has = items != null && items.size() > 0;
        return DataResult.of((long) (has ? items.size() : 0), items);
    }
}
