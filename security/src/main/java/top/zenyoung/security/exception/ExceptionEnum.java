package top.zenyoung.security.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.boot.exception.ServiceException;
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
    LOGIN_OTHER(5300, "您的账号已在别处登录");

    private final int val;
    private final String title;

    public ServiceException exception() {
        return new ServiceException(this);
    }
}
