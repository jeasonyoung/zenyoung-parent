package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.common.model.Status;
import top.zenyoung.framework.dto.BasePageDTO;

/***
 * 用户-查询DTO
 * @author young
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends BasePageDTO {
    /**
     * 用户姓名/账号/手机号码
     */
    @ApiModelProperty("用户姓名/账号/手机号码")
    private String name;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
    /**
     * 所属部门ID
     */
    @ApiModelProperty("所属部门ID")
    private Long deptId;
}
