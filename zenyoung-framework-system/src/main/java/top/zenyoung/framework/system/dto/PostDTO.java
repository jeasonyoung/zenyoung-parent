package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDTO extends PostBaseDTO {
    /**
     * 岗位ID
     */
    @ApiModelProperty("岗位ID")
    private Long id;
    /**
     * 所属部门
     */
    @ApiModelProperty("所属部门")
    private DeptInfoDTO dept;
}
