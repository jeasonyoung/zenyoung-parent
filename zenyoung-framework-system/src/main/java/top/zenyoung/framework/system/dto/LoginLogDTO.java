package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志-数据DTO
 *
 * @author young
 */
@Data
@ApiModel("登录日志-数据")
@EqualsAndHashCode(callSuper = true)
public class LoginLogDTO extends LoginLogBaseDTO {
    /**
     * 登录日志ID
     */
    @ApiModelProperty("登录日志ID")
    private Long id;
}
