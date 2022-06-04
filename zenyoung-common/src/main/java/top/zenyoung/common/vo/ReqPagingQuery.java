package top.zenyoung.common.vo;

import lombok.Data;
import top.zenyoung.common.paging.PagingQuery;

/**
 * 分页-请求报文体
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/9 10:56 下午
 **/
@Data
public class ReqPagingQuery implements PagingQuery {
    /**
     * 页码
     */
    private Integer pageIndex;
    /**
     * 当前页数量
     */
    private Integer pageSize;
}
