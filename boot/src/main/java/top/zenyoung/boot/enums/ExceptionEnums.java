package top.zenyoung.boot.enums;

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
public enum ExceptionEnums implements EnumValue {
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),
    /**
     * 拒绝访问
     */
    FORBIDDEN(403, "拒绝访问"),
    /**
     * 令牌无效
     */
    INVALID_TOKEN(1401, "令牌无效"),
    /**
     * 令牌非法
     */
    ILLEGAL_TOKEN(1402, "令牌非法"),
    /**
     * 账号不存在
     */
    ACCOUNT_NOT(1403, "账号不存在"),
    /**
     * 账号无效
     */
    ACCOUNT_INVALID(1404, "账号无效"),
    ;

    private final int val;
    private final String title;
}
