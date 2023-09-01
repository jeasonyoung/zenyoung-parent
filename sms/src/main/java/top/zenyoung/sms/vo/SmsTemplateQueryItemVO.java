package top.zenyoung.sms.vo;

import lombok.Data;
import top.zenyoung.sms.enums.SmsAuditStatus;
import top.zenyoung.sms.enums.SmsTemplateType;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信模板-查询项目VO
 *
 * @author yangyong
 */
@Data
public class SmsTemplateQueryItemVO implements Serializable {
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板Code
     */
    private String templateCode;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 模板类型
     */
    private SmsTemplateType type;
    /**
     * 审核状态
     */
    private SmsAuditStatus auditStatus;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 审核备注
     */
    private String reason;
}
