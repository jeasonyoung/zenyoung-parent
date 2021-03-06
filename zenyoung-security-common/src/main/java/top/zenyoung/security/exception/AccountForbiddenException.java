package top.zenyoung.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 异常-账号已禁用
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/14 6:07 下午
 **/
public class AccountForbiddenException extends AuthenticationException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public AccountForbiddenException(final String message) {
        super(message);
    }
}
