package top.zenyoung.common.paging;

import java.io.Serializable;

/**
 * 分页数据-查询
 *
 * @author yangyong
 * @version 1.0
 **/
public interface PagingQuery extends Serializable {
    /**
     * 获取页码
     *
     * @return 页码
     */
    Integer getPageIndex();

    /**
     * 获取每页数据量
     *
     * @return 每页数据量
     */
    Integer getPageSize();
}
