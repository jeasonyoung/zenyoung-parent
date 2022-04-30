package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;
import top.zenyoung.framework.system.model.MenuType;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 菜单-基础DTO
 */
@Data
class MenuBaseDTO implements Serializable {
    /**
     * 菜单代码(排序)
     */
    @ApiModelProperty("菜单代码")
    private Integer code;
    /**
     * 菜单名称
     */
    @ApiModelProperty("菜单名称")
    @NotBlank(groups = {Insert.class}, message = "菜单名称不能为空")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "菜单名称长度不能超过128个字符")
    private String name;
    /**
     * 路由地址
     */
    @ApiModelProperty("路由地址")
    private String path;
    /**
     * 组件路径
     */
    @ApiModelProperty("组件路径")
    private String component;
    /**
     * 路由参数
     */
    @ApiModelProperty("路由参数")
    private String query;
    /**
     * 是否为外链(0:否,1:是)
     */
    @ApiModelProperty("是否为外链(0:否,1:是)")
    private Integer isLink;
    /**
     * 是否缓存(0:不缓存,1:缓存)
     */
    @ApiModelProperty("是否缓存(0:不缓存,1:缓存)")
    private Integer isCache;
    /**
     * 菜单类型(1:目录,2:菜单,3:按钮)
     */
    @ApiModelProperty("菜单类型")
    @NotNull(groups = {Insert.class}, message = "菜单类型")
    private MenuType type;
    /**
     * 菜单状态(1:显示,0:隐藏)
     */
    @ApiModelProperty("菜单状态(1:显示,0:隐藏)")
    private Integer visible;
    /**
     * 权限标识
     */
    @ApiModelProperty("权限标识")
    private String perms;
    /**
     * 菜单图标
     */
    @ApiModelProperty("菜单图标")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "菜单图标长度不超过128个字符")
    private String icon;
}
