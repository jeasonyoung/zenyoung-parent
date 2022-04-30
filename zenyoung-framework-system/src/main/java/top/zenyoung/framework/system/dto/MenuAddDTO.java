package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单-新增DTO
 * @author young
 */
@Data
@ApiModel("菜单-新增")
@EqualsAndHashCode(callSuper = true)
public class MenuAddDTO extends MenuBaseDTO{
    /**
     * 父菜单ID
     */
    @ApiModelProperty("父菜单ID")
    private Long parentId;
}
