package top.zenyoung.sms.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 短信模板类型-枚举
 *
 * @author yangyong
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SmsTemplateType {
    /**
     * 验证码
     */
    CODE,
    /**
     * 短信通知
     */
    NOTICE,
    /**
     * 推广短信
     */
    PROMOTION,
    /**
     * 国际/港澳台
     */
    INTERNATIONAL;
}
