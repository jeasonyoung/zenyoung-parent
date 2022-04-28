package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.dto.BasePageDTO;

/**
 * 岗位-查询DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostQueryDTO extends BasePageDTO {
    /**
     * 岗位编码/岗位名称
     */
    @ApiModelProperty("岗位编码/岗位名称")
    private String name;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
    /**
     * 所属部门ID
     */
    @ApiModelProperty("所属部门ID")
    private Long deptId;
}
