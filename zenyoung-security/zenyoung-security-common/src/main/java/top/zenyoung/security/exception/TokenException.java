package top.zenyoung.security.exception;

/**
 * 令牌异常
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/19 5:01 下午
 **/
public class TokenException extends RuntimeException {

    public TokenException(final String message) {
        super(message);
    }

    public TokenException(final Throwable cause) {
        super(cause);
    }
}
