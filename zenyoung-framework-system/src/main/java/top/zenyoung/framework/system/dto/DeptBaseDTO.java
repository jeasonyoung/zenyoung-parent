package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Insert;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 部门-基础DTO
 *
 * @author young
 */
@Data
class DeptBaseDTO implements Serializable {
    /**
     * 部门代码(排序)
     */
    @ApiModelProperty("部门代码(排序)")
    private Integer code;
    /**
     * 部门名称
     */
    @ApiModelProperty("部门名称")
    @NotEmpty(groups = {Insert.class}, message = "'name'不能为空!")
    private String name;
    /**
     * 负责人
     */
    @ApiModelProperty("负责人")
    private String leader;
    /**
     * 联系电话
     */
    @ApiModelProperty("联系电话")
    private String mobile;
    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    private String email;
}
