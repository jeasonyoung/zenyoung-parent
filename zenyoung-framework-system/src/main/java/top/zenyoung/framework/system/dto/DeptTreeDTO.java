package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 部门-树DTO
 *
 * @author young
 */
@Data
@ApiModel("部门-树")
@EqualsAndHashCode(callSuper = true)
public class DeptTreeDTO extends DeptDTO {
    /**
     * 子部门集合
     */
    @ApiModelProperty("子部门集合")
    private List<DeptTreeDTO> children;
}
