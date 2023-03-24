package top.zenyoung.common.paging;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据列表
 *
 * @author young
 */
public interface PageList<T> extends Serializable {
    /**
     * 空数据列表
     */
    PageList<?> EMPTY = DataResult.empty();

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
    List<T> getRows();
}
