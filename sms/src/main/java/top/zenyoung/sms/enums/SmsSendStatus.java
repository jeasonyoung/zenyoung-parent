package top.zenyoung.sms.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 短信发送状态
 *
 * @author yangyong
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SmsSendStatus {
    /**
     * 等待回执
     */
    WAIT,
    /**
     * 发送失败
     */
    FAIL,
    /**
     * 发送成功
     */
    SUCCESS
}
