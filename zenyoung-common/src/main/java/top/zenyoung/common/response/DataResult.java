package top.zenyoung.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zenyoung.common.paging.PagingResult;

import java.io.Serializable;
import java.util.List;

/**
 * 数据结果
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/2 5:51 下午
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResult<T extends Serializable> implements PagingResult<T>, Serializable {

    /**
     * 数据总数
     */
    private Long totals;
    /**
     * 数据集合
     */
    private List<T> rows;
}
