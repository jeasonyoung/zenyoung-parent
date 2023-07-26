package top.zenyoung.sms.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.zenyoung.sms.enums.SmsAuditStatus;

import java.util.Date;

/**
 * 短信签名-新增状态VO
 *
 * @author yangyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSignAddStatusVO extends SmsSignAddVO {
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
