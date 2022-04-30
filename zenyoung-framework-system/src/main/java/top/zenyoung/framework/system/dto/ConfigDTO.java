package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;

/**
 * 参数配置-加载DTO
 *
 * @author young
 */
@Data
@ApiModel("参数配置-数据")
@EqualsAndHashCode(callSuper = true)
public class ConfigDTO extends ConfigBaseDTO {
    /***
     * 参数ID
     */
    @ApiModelProperty("参数ID")
    private Long id;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
