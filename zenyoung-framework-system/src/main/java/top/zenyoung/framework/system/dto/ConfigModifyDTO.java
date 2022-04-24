package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;

/**
 * 参数配置-修改DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigModifyDTO extends ConfigAddDTO {
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
