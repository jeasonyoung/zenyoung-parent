package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.dto.BasePageDTO;

/**
 * 参数配置-查询DTO
 *
 * @author young
 */
@Data
@ApiModel("参数配置-查询")
@EqualsAndHashCode(callSuper = true)
public class ConfigQueryDTO extends BasePageDTO {
    /**
     * 参数名
     */
    @ApiModelProperty("参数名")
    private String name;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
