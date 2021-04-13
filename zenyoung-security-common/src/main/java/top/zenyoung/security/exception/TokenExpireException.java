package top.zenyoung.security.exception;

/**
 * 令牌过期-异常
 *
 * @author young
 */
public class TokenExpireException extends TokenException {

    public TokenExpireException(String message) {
        super(message);
    }

    public TokenExpireException(Throwable cause) {
        super(cause);
    }
}