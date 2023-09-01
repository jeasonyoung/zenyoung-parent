package top.zenyoung.sms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import top.zenyoung.common.dto.BasePageDTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 短信发送详情-查询DTO
 *
 * @author yangyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSendQueryDetailDTO extends BasePageDTO {
    /**
     * 手机号码
     */
    @NotBlank(message = "'mobile'不能为空")
    private String mobile;
    /**
     * 回执ID
     */
    private String bizId;
    /**
     * 发送日期(yyyy-MM-dd)
     */
    @NotNull(message = "'sendDate'不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date sendDate;
}
