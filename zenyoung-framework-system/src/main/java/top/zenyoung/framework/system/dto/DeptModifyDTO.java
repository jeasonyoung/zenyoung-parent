package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.valid.Modify;

import javax.validation.constraints.NotNull;

/**
 * 部门-修改TO
 *
 * @author young
 */
@Data
@ApiModel("部门-修改")
@EqualsAndHashCode(callSuper = true)
public class DeptModifyDTO extends DeptAddDTO {
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
