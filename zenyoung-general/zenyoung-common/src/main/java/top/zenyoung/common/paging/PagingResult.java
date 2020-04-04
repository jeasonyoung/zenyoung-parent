package top.zenyoung.common.paging;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据-查询结果
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/2/6 5:10 下午
 **/
public interface PagingResult<T extends Serializable> extends Serializable {
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