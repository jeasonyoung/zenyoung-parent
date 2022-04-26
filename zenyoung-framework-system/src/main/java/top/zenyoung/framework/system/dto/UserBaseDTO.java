package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户-基础DTO
 *
 * @author young
 */
@Data
public class UserBaseDTO implements Serializable {
    /**
     * 用户姓名
     */
    @ApiModelProperty("用户姓名")
    @NotBlank(groups = Insert.class, message = "用户姓名不能为为空!")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "用户名长度不能超过32位字符!")
    private String name;
    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    @NotBlank(groups = Insert.class, message = "用户账号不能为为空!")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "用户账号长度不能超过32位字符!")
    private String account;
    /**
     * 联系电话
     */
    @ApiModelProperty("联系电话")
    @Max(groups = {Insert.class, Modify.class}, value = 20, message = "联系电话长度不能超过20位字符!")
    private String mobile;
    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "邮箱长度不能超过128位字符!")
    private String email;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
