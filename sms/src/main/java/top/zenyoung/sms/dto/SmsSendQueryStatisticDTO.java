package top.zenyoung.sms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import top.zenyoung.common.dto.BasePageDTO;
import top.zenyoung.sms.enums.SmsSendRange;
import top.zenyoung.sms.enums.SmsTemplateType;

import java.util.Date;

/**
 * 短信发送统计-查询DTO
 *
 * @author yangyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSendQueryStatisticDTO extends BasePageDTO {
    /**
     * 签名名称
     */
    private String sign;
    /**
     * 模板类型
     */
    private SmsTemplateType templateType;
    /**
     * 发送范围
     */
    private SmsSendRange range = SmsSendRange.INTERNAL;
    /**
     * 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date start;
    /**
     * 结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date end;
}
