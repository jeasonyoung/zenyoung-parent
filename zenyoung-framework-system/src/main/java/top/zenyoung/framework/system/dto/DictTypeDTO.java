package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型-数据DTO
 *
 * @author young
 */
@Data
@ApiModel("字典类型-数据")
@EqualsAndHashCode(callSuper = true)
public class DictTypeDTO extends DictTypeBaseDTO {
    /**
     * 字典类型ID
     */
    @ApiModelProperty("字典类型ID")
    private Long id;
}
