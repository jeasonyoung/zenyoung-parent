package top.zenyoung.common.paging;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/18 2:59 下午
 **/
public interface PagingResult<T extends Serializable> {
    /**
     * 获取数据总数
     *
     * @return 数据总数
     */
    Long getTotals();

    /**
     * 获取查询数据结果集合
     *
     * @return 数据结果集合
     */
    List<T> getRows();
}
