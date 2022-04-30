package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-新增DTO
 *
 * @author young
 */
@Data
@ApiModel("角色-新增")
@EqualsAndHashCode(callSuper = true)
public class RoleAddDTO extends RoleBaseDTO {

}
