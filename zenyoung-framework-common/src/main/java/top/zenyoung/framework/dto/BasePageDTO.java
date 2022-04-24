package top.zenyoung.framework.dto;

import lombok.Data;
import top.zenyoung.common.paging.PagingQuery;

/**
 * 分页抽象基类DTO
 *
 * @author young
 */
@Data
public abstract class BasePageDTO implements PagingQuery {
    private static final int DEF_PAGE_INDEX = 1;
    private static final int DEF_PAGE_SIZE = 20;
    /**
     * 页码
     */
    private Integer pageIndex;
    /**
     * 页数据量
     */
    private Integer pageSize;

    /**
     * 构造函数
     */
    public BasePageDTO() {
        this.pageIndex = DEF_PAGE_INDEX;
        this.pageSize = DEF_PAGE_SIZE;
    }
}
