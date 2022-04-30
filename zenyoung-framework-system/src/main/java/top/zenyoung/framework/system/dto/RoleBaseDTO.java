package top.zenyoung.framework.system.dto;

import lombok.Data;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;
import top.zenyoung.framework.system.model.DataScope;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 角色-基础DTO
 *
 * @author young
 */
@Data
class RoleBaseDTO implements Serializable {
    /**
     * 角色代码(排序)
     */
    private Integer code;
    /**
     * 角色名称
     */
    @NotBlank(groups = {Insert.class}, message = "角色名称不能为空!")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "角色名称长度不能超过32位")
    private String name;
    /**
     * 角色简称
     */
    @NotBlank(groups = {Insert.class}, message = "角色简称不能为空!")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "角色简称长度不能超过32位")
    private String abbr;
    /**
     * 角色备注
     */
    private String remark;
    /**
     * 数据权限范围
     */
    private DataScope scope;
}
