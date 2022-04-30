package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.dto.BasePageDTO;

/**
 * 菜单-查询DTO
 *
 * @author young
 */
@Data
@ApiModel("菜单-查询")
@EqualsAndHashCode(callSuper = true)
public class MenuQueryDTO extends BasePageDTO {
    /**
     * 菜单名称/菜单代码/权限标识
     */
    @ApiModelProperty("菜单名称/菜单代码/权限标识")
    private String name;
    /**
     * 父菜单ID
     */
    @ApiModelProperty("父菜单ID")
    private Long parentId;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
