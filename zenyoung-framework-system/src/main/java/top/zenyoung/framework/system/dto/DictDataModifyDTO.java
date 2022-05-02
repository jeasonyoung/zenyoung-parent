package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;

/**
 * 字典数据-修改DTO
 *
 * @author young
 */
@Data
@ApiModel("字典数据-修改")
@EqualsAndHashCode(callSuper = true)
public class DictDataModifyDTO extends DictDataBaseDTO {
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
