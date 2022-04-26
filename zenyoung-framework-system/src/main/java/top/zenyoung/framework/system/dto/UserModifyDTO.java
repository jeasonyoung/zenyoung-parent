package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户-修改DTO
 *
 * @author young
 */
@Data
@ApiModel("用户修改数据")
@EqualsAndHashCode(callSuper = true)
public class UserModifyDTO extends UserBaseDTO {
    /**
     * 密码
     */
    @ApiModelProperty("密码")
    @NotBlank(groups = Insert.class, message = "密码不能为空!")
    @Max(groups = {Insert.class, Modify.class}, value = 64, message = "密码长度不能超过32位字符!")
    private String passwd;
    /**
     * 所属部门ID
     */
    @ApiModelProperty("所属部门ID")
    @NotNull(groups = Insert.class, message = "所属部门ID不能为空!")
    private Long deptId;
    /**
     * 所属岗位集合
     */
    @ApiModelProperty("所属岗位集合")
    private List<Long> posts;
    /**
     * 所属角色集合
     */
    @ApiModelProperty("所属角色集合")
    private List<Long> roles;
}
