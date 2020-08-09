package top.zenyoung.web.vo;

import lombok.Data;
import top.zenyoung.common.paging.PagingQuery;

import java.io.Serializable;

/**
 * 分页-请求报文体
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/8/9 10:56 下午
 **/
@Data
public class ReqPagingQuery<T extends Serializable> implements PagingQuery<T> {
    /**
     * 页码
     */
    private Integer index;
    /**
     * 当前页数量
     */
    private Integer rows;
    /**
     * 查询条件
     */
    private T query;
}
