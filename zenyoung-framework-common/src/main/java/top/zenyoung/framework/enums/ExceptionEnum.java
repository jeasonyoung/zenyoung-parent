package top.zenyoung.framework.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.model.EnumValue;

/**
 * 异常枚举
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExceptionEnum implements EnumValue {
    /**
     * 验证码错误
     */
    CODE_ERROR(5031, "验证码错误"),
    /**
     * 手机号码格式不正确
     */
    MOBILE_ERROR(5044, "手机号码格式不正确"),
    /**
     * 登录已失效,请重新登录
     */
    TOKEN_ERROR(5134, "登录已失效,请重新登录"),
    /**
     * token已过期
     */
    TOKEN_EXPIRE(5135, "token已过期"),
    /**
     * refreshToken已过期
     */
    REFRESH_EXPIRE(5136, "refreshToken已过期"),
    /**
     * 您的账号已在别处登录
     */
    LOGIN_OTHER(5300, "您的账号已在别处登录"),
    /**
     * 非法请求
     */
    ILLEGAL_REQUEST(5302, "非法请求"),
    ;

    private final int val;
    private final String title;
}
