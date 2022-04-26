package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 部门-信息DTO
 *
 * @author young
 */
@Data
@AllArgsConstructor(staticName = "of")
public class DeptInfoDTO {
    /**
     * 部门ID
     */
    @ApiModelProperty("部门ID")
    private Long id;
    /**
     * 部门名称
     */
    @ApiModelProperty("部门名称")
    private String name;
}
