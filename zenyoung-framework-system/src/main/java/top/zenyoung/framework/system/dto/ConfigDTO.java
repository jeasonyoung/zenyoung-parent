package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.system.model.ConfigType;

import java.io.Serializable;

/**
 * 参数配置-加载DTO
 *
 * @author young
 */
@Data
@ApiModel("参数配置-加载")
public class ConfigDTO implements Serializable {
    /***
     * 参数ID
     */
    @ApiModelProperty("参数ID")
    private Long id;
    /**
     * 参数名称
     */
    @ApiModelProperty("参数名称")
    private String name;
    /**
     * 参数键名
     */
    @ApiModelProperty("参数键名")
    private String key;
    /**
     * 参数键值
     */
    @ApiModelProperty("参数键值")
    private String val;
    /**
     * 参数类型
     */
    @ApiModelProperty("参数类型")
    private ConfigType type;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
