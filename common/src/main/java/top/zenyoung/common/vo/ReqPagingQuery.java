package top.zenyoung.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.common.paging.PagingQuery;

/**
 * 分页-请求报文体
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/9 10:56 下午
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqPagingQuery extends BasePageDTO implements PagingQuery {
    /**
     * 页码
     */
    private Integer pageIndex;
    /**
     * 页数据量
     */
    private Integer pageSize;
}
