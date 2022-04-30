package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-加载DTO
 *
 * @author young
 */
@Data
@ApiModel("角色数据")
@EqualsAndHashCode(callSuper = true)
public class RoleDTO extends RoleBaseDTO {
    /**
     * 角色ID
     */
    @ApiModelProperty("角色ID")
    private Long id;
}
