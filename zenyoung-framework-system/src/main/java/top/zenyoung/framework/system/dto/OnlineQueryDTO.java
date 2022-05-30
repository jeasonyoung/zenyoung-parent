package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.framework.dto.BasePageDTO;

/**
 * 在线用户-查询DTO
 *
 * @author young
 */
@Data
@ApiModel("在线用户-查询")
@EqualsAndHashCode(callSuper = true)
public class OnlineQueryDTO extends BasePageDTO {
    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    private String account;
}
