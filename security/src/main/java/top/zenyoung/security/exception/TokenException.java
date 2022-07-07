package top.zenyoung.security.exception;

import com.google.common.base.Strings;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import top.zenyoung.common.model.EnumValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 令牌异常
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 5:01 下午
 **/
@Getter
public class TokenException extends AuthenticationException {
    /**
     * 错误代码
     */
    private final Integer code;

    public TokenException(@Nonnull final EnumValue ev, @Nullable final String message) {
        super(Strings.isNullOrEmpty(message) ? ev.getTitle() : message);
        this.code = ev.getVal();
    }

    public TokenException(@Nullable final String message) {
        this(ExceptionEnum.TOKEN_ERROR, message);
    }

    public TokenException(@Nullable final Throwable cause) {
        this(ExceptionEnum.TOKEN_ERROR, cause);
    }

    public TokenException(@Nonnull final EnumValue ev, @Nullable final Throwable cause) {
        this(ev, cause == null ? null : cause.getMessage());
    }
}
