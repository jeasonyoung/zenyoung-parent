package top.zenyoung.sms.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 短信签名-审核状态
 *
 * @author yangyong
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SmsAuditStatus {
    /**
     * 审核中
     */
    INIT,
    /**
     * 审核通过
     */
    PASS,
    /**
     * 审核未通过
     */
    NOT_PASS,
    /**
     * 取消审核
     */
    CANCEL
}
