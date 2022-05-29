package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录日志-基础DTO
 */
@Data
class LoginLogBaseDTO implements Serializable {
    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    @NotBlank(groups = {Insert.class}, message = "用户ID不能为空")
    private Long userId;
    /**
     * 用户账号
     */
    @ApiModelProperty("用户账号")
    @NotBlank(groups = {Insert.class}, message = "用户账号不能为空")
    @Max(groups = {Insert.class, Modify.class}, value = 255, message = "用户账号长度不超过255个字符")
    private String account;
    /**
     * 登录IP地址
     */
    @ApiModelProperty("登录IP地址")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "登录IP地址长度不超过32个字符")
    private String ipAddr;
    /**
     * 登录地点
     */
    @ApiModelProperty("登录地点")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "登录地点长度不超过128个字符")
    private String ipLocation;
    /**
     * 浏览器类型
     */
    @ApiModelProperty("浏览器类型")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "浏览器类型长度不超过128个字符")
    private String browser;
    /**
     * 操作系统
     */
    @ApiModelProperty("操作系统")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "操作系统长度不超过128个字符")
    private String os;
    /**
     * 客户端设备
     */
    @ApiModelProperty("客户端设备")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "客户端设备长度不超过128个字符")
    private String device;
    /**
     * 提示消息
     */
    @ApiModelProperty("提示消息")
    private String msg;
}
