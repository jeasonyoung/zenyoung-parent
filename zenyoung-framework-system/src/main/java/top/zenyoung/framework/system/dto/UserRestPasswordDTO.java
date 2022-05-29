package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Modify;

import javax.validation.constraints.NotNull;

/**
 * 用户重置密码-数据DTO
 *
 * @author young
 */
@Data
@ApiModel("用户重置密码")
public class UserRestPasswordDTO {
    /**
     * 旧密码
     */
    @ApiModelProperty("旧密码")
    @NotNull(groups = {Modify.class}, message = "旧密码不能为空!")
    private String oldPwd;
    /**
     * 新密码
     */
    @ApiModelProperty("新密码")
    @NotNull(groups = {Modify.class}, message = "新密码不能为空!")
    private String newPwd;
}
