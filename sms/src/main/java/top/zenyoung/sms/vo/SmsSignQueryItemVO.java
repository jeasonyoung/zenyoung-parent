package top.zenyoung.sms.vo;

import lombok.Data;
import top.zenyoung.sms.enums.SmsAuditStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信签名-查询项目VO
 *
 * @author yangyong
 */
@Data
public class SmsSignQueryItemVO implements Serializable {
    /**
     * 签名
     */
    private String sign;
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
