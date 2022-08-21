package top.zenyoung.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.zenyoung.common.paging.PagingQuery;

/**
 * 分页抽象基类DTO
 *
 * @author young
 */
@Data
@AllArgsConstructor(staticName = "of")
public class BasePageDTO implements PagingQuery {
    public static final int DEF_PAGE_INDEX = 1;
    public static final int DEF_PAGE_SIZE = 20;
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
