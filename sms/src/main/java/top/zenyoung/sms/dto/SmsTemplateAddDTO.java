package top.zenyoung.sms.dto;

import lombok.Data;
import top.zenyoung.sms.enums.SmsTemplateType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 短信模板-新增DTO
 *
 * @author yangyong
 */
@Data
public class SmsTemplateAddDTO {
    /**
     * 模板名称
     */
    @NotBlank(message = "'name'不能为空")
    private String name;
    /**
     * 模板类型
     */
    @NotNull(message = "'type'不能为空")
    private SmsTemplateType type = SmsTemplateType.NOTICE;
    /**
     * 模板内容
     */
    @NotBlank(message = "'content'不能为空")
    private String content;
    /**
     * 模板说明
     */
    private String remark;
}
