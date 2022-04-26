package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户-数据DTO
 *
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends UserBaseDTO {
    /**
     * 用户ID
     */
    @ApiModelProperty("用户ID")
    private Long id;
    /**
     * 所属部门
     */
    @ApiModelProperty("所属部门")
    private DeptInfoDTO dept;
    /**
     * 所属岗位集合
     */
    @ApiModelProperty("所属岗位")
    private List<PostInfoDTO> posts;
    /**
     * 所属角色集合
     */
    @ApiModelProperty("所属角色集合")
    private List<RoleInfoDTO> roles;
}
