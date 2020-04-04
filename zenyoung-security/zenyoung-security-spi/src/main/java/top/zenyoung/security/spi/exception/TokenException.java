package top.zenyoung.security.spi.exception;

import org.springframework.security.core.AuthenticationException;

import javax.annotation.Nonnull;

/**
 * 令牌异常
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:01 下午
 **/
public class TokenException extends AuthenticationException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public TokenException(@Nonnull final String message) {
        super(message);
    }
}
