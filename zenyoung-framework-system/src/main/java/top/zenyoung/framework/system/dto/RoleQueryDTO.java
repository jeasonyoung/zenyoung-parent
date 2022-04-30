package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.dto.BasePageDTO;

/**
 * 角色-查询DTO
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends BasePageDTO {
    /**
     * 角色代码/名称/简称
     */
    @ApiModelProperty("角色代码/名称/简称")
    private String name;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
