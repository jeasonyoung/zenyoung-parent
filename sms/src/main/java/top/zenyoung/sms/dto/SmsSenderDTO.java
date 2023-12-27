package top.zenyoung.sms.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;

/**
 * 短信发送DTO
 *
 * @author young
 */
@Data
public class SmsSenderDTO implements Serializable {
    /**
     * 模板
     */
    @NotBlank(message = "'template'不能为空")
    private String template;
    /**
     * 模板参数
     */
    private Map<String, Object> data;
    /**
     * 签名
     */
    private String signName;
    /**
     * 接收人手机(多个手机号可用,分隔,最大不超过1000个)
     */
    @NotBlank(message = "'mobile'不能为空")
    private String mobile;
    /**
     * 上行短信扩展码
     */
    private String smsUpExtendCode;
    /**
     * 回执id
     */
    private String outId;
}
