package top.zenyoung.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 令牌异常
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 5:01 下午
 **/
public class TokenException extends AuthenticationException {

    public TokenException(final String message) {
        super(message);
    }

    public TokenException(final Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }
}
