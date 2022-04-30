package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.framework.system.model.ConfigType;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 参数配置-基础DTO
 */
@Data
class ConfigBaseDTO implements Serializable {
    /**
     * 参数名称
     */
    @ApiModelProperty("参数名称")
    @NotBlank(groups = {Insert.class}, message = "参数名称不能为空!")
    private String name;
    /**
     * 参数键名
     */
    @ApiModelProperty("参数键名")
    @NotBlank(groups = {Insert.class}, message = "参数键名不能为空!")
    private String key;
    /**
     * 参数键值
     */
    @ApiModelProperty("参数键值")
    @NotBlank(groups = {Insert.class}, message = "参数键值不能为空!")
    private String val;
    /**
     * 参数类型
     */
    @ApiModelProperty("参数类型")
    private ConfigType type;
}
