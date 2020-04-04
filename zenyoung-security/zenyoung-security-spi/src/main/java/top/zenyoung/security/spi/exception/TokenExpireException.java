package top.zenyoung.security.spi.exception;

import javax.annotation.Nonnull;

/**
 * 令牌过期异常
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:03 下午
 **/
public class TokenExpireException extends TokenException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public TokenExpireException(@Nonnull final String message) {
        super(message);
    }
}
