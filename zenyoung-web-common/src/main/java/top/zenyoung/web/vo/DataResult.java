package top.zenyoung.web.vo;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.paging.PagingResult;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * 数据结果
 *
 * @author yangyong
 * @version 1.0
 **/
@Getter
@RequiredArgsConstructor(staticName = "of")
public class DataResult<T extends Serializable> implements PagingResult<T> {
    /**
     * 数据总数
     */
    private final Long total;
    /**
     * 数据集合
     */
    private final List<T> rows;

    public static <T extends Serializable> DataResult<T> of(@Nullable final PagingResult<T> data) {
        return new DataResult<>(data == null ? 0L : data.getTotal(), data == null ? Lists.newLinkedList() : data.getRows());
    }
}
