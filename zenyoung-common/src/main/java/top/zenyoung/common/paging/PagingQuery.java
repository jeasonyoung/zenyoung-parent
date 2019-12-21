package top.zenyoung.common.paging;

import java.io.Serializable;

/**
 * 分页查询条件
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/18 2:58 下午
 **/
public interface PagingQuery<T extends Serializable> {
    /**
     * 获取页码
     *
     * @return 页码
     */
    Integer getIndex();

    /**
     * 获取每页数据量
     *
     * @return 每页数据量
     */
    Integer getRows();

    /**
     * 获取查询条件
     *
     * @return 查询条件
     */
    T getQuery();
}
