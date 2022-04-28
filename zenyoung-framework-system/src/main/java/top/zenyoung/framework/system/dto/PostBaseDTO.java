package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 岗位-基础DTO
 *
 * @author young
 */
@Data
public class PostBaseDTO implements Serializable {
    /**
     * 岗位编码
     */
    @NotBlank(groups = Insert.class, message = "岗位编码不能为空")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "岗位编码长度不能超过32位")
    private String code;
    /**
     * 岗位名称
     */
    @NotBlank(groups = Insert.class, message = "岗位名称不能为空")
    @Max(groups = {Insert.class, Modify.class}, value = 32, message = "岗位名称长度不能超过32位")
    private String name;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Status status;
}
