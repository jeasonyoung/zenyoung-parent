package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志-新增DTO
 *
 * @author young
 */
@Data
@ApiModel("登录日志-新增")
@EqualsAndHashCode(callSuper = true)
public class LoginLogAddDTO extends LoginLogBaseDTO {
    
}
