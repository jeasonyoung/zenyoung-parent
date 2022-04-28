package top.zenyoung.framework.system.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.valid.Insert;

import javax.validation.constraints.NotNull;

/**
 * 岗位-修改DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostModifyDTO extends PostBaseDTO {
    /**
     * 所属部门ID
     */
    @NotNull(groups = {Insert.class}, message = "所属部门ID不能为空!")
    private Long deptId;
}
