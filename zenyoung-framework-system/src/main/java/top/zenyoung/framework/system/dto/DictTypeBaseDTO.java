package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 字典类型-数据DTO
 *
 * @author young
 */
@Data
class DictTypeBaseDTO implements Serializable {
    /**
     * 字典名称
     */
    @ApiModelProperty("字典名称")
    @NotBlank(groups = {Insert.class}, message = "字典名称不能为空")
    @Max(groups = {Insert.class, Modify.class}, value = 64, message = "字典名称长度不能超过64个字符")
    private String name;
    /**
     * 字典类型
     */
    @ApiModelProperty("字典类型")
    @NotBlank(groups = {Insert.class}, message = "字典类型不能为空")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "字典类型长度不能超过128个字符")
    private String type;
    /**
     * 字典备注
     */
    @ApiModelProperty("字典备注")
    private String remark;
}
