package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单-数据DTO
 *
 * @author young
 */
@Data
@ApiModel("菜单-数据")
@EqualsAndHashCode(callSuper = true)
public class MenuDTO extends MenuBaseDTO {
    /**
     * 菜单ID
     */
    @ApiModelProperty("菜单ID")
    private Long id;
    /**
     * 父菜单ID
     */
    @ApiModelProperty("父菜单ID")
    private Long parentId;
}
