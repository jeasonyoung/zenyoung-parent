package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门-新增DTO
 *
 * @author young
 */
@Data
@ApiModel("部门-新增")
@EqualsAndHashCode(callSuper = true)
public class DeptAddDTO extends DeptBaseDTO {
    /**
     * 上级部门ID
     */
    @ApiModelProperty("上级部门ID")
    private Long parentId;
}
