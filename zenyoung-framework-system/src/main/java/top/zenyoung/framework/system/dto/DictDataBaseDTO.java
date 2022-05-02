package top.zenyoung.framework.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.zenyoung.common.valid.Insert;
import top.zenyoung.common.valid.Modify;

import javax.persistence.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 字典数据-基础DTO
 */
@Data
class DictDataBaseDTO implements Serializable {
    /**
     * 字典代码(排序)
     */
    @ApiModelProperty("字典代码")
    private Integer code;
    /**
     * 字典标签
     */
    @NotBlank(groups = {Insert.class}, message = "字典标签不能为空")
    @ApiModelProperty("字典标签")
    private String label;
    /**
     * 字典键值
     */
    @NotBlank(groups = {Insert.class}, message = "字典键值不能为空")
    @ApiModelProperty("字典键值")
    private String value;
    /**
     * 是否默认(0:否,1:是)
     */
    @ApiModelProperty("是否默认(0:否,1:是)")
    private Integer isDefault;
    /**
     * 样式属性
     */
    @ApiModelProperty("样式属性")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "样式属性长度不超过128个字符")
    private String cssClass;
    /**
     * 表格回显样式
     */
    @ApiModelProperty("表格回显样式")
    @Max(groups = {Insert.class, Modify.class}, value = 128, message = "表格回显样式不超过128个字符")
    private String listClass;
    /**
     * 备注
     */
    @Column
    private String remark;
}
