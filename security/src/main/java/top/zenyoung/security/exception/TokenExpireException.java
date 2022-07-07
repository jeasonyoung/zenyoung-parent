package top.zenyoung.security.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 令牌过期-异常
 *
 * @author young
 */
public class TokenExpireException extends TokenException {

    public TokenExpireException(@Nonnull final String message) {
        super(ExceptionEnum.TOKEN_EXPIRE, message);
    }

    public TokenExpireException(@Nullable final Throwable cause) {
        super(ExceptionEnum.TOKEN_EXPIRE, cause);
    }
}