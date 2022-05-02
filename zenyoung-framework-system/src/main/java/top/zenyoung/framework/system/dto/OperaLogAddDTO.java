package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作记录-新增DTO
 *
 * @author young
 */
@Data
@ApiModel("操作记录-新增")
@EqualsAndHashCode(callSuper = true)
public class OperaLogAddDTO extends OperaLogBaseDTO {
}
