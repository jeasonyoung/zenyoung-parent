package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据-数据DTO
 *
 * @author young
 */
@Data
@ApiModel("字典数据-数据")
@EqualsAndHashCode(callSuper = true)
public class DictDataDTO extends DictDataBaseDTO {
    /**
     * 字典数据ID
     */
    @ApiModelProperty("字典数据ID")
    private Long id;
}
