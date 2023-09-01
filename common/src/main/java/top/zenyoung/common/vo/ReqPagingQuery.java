package top.zenyoung.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import top.zenyoung.common.dto.BasePageDTO;

/**
 * 分页-请求报文体
 *
 * @author yangyong
 * @version 1.0
 * date 2020/8/9 10:56 下午
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqPagingQuery extends BasePageDTO {

}
