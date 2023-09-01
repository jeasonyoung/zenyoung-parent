package top.zenyoung.sms.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 短信发送范围-枚举
 *
 * @author yangyong
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SmsSendRange {
    /**
     * 国内短信发送记录
     */
    INTERNAL,
    /**
     * 国际/港澳台
     */
    INTERNATIONAL
}
