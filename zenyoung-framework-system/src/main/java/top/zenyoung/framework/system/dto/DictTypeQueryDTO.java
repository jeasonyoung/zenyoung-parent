package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.dto.BasePageDTO;

/**
 * 字典类型-查询DTO
 * @author young
 */
@Data
@ApiModel("字典类型-查询")
@EqualsAndHashCode(callSuper = true)
public class DictTypeQueryDTO extends BasePageDTO {
    /**
     * 字典名称/类型
     */
    @ApiModelProperty("字典名称/类型")
    private String name;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
