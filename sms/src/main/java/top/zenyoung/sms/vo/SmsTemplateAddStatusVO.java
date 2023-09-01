package top.zenyoung.sms.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.sms.enums.SmsAuditStatus;
import top.zenyoung.sms.enums.SmsTemplateType;

import java.util.Date;

/**
 * 短信模板-申请状态VO
 *
 * @author yangyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsTemplateAddStatusVO extends SmsTemplateAddVO {
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 模板类型
     */
    private SmsTemplateType type;
    /**
     * 模板审核状态
     */
    private SmsAuditStatus templateStatus;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 审核备注
     */
    private String reason;
}
