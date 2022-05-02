package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作记录-数据DTO
 * @author young
 */
@Data
@ApiModel("操作记录-数据")
@EqualsAndHashCode(callSuper = true)
public class OperaLogDTO extends OperaLogBaseDTO {
    /**
     * 操作记录ID
     */
    @ApiModelProperty("操作记录ID")
    private Long id;
}
