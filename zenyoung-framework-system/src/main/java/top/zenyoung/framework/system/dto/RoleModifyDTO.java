package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-修改DTO
 *
 * @author young
 */
@Data
@ApiModel("角色修改")
@EqualsAndHashCode(callSuper = true)
public class RoleModifyDTO extends RoleBaseDTO {
}
