package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.web.valid.Modify;

import javax.validation.constraints.NotNull;

/**
 * 部门-修改-数据DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptModifyDTO extends DeptAddDTO {
    /**
     * 部门ID
     */
    @NotNull(groups = {Modify.class}, message = "'id'不能为空!")
    private Long id;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
