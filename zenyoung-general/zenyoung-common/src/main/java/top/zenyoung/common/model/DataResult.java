package top.zenyoung.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResult<T extends Serializable> implements PagingResult<T> {
    /**
     * 数据总数
     */
    private Long total;

    /**
     * 数据集合
     */
    private List<T> rows;

    /**
     * 构造函数
     *
     * @param data 分页数据
     */
    public DataResult(@Nullable final PagingResult<T> data) {
        if (data != null) {
            setTotal(data.getTotal());
            setRows(data.getRows());
        }
    }

    /**
     * 静态构建数据结果
     *
     * @param data 分页接口数据
     * @param <T>  数据类型
     * @return 数据结果
     */
    public static <T extends Serializable> DataResult<T> of(@Nullable final PagingResult<T> data) {
        return new DataResult<>(data);
    }
}
