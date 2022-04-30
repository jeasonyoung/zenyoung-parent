package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门-数据DTO
 *
 * @author young
 */
@Data
@ApiModel("部门-数据")
@EqualsAndHashCode(callSuper = true)
public class DeptDTO extends DeptBaseDTO {
    /**
     * 部门ID
     */
    @ApiModelProperty("部门ID")
    private Long id;
    /**
     * 上级部门ID
     */
    @ApiModelProperty("上级部门ID")
    private Long parentId;
    /**
     * 祖级列表
     */
    @ApiModelProperty("祖级列表")
    private String ancestors;
}
