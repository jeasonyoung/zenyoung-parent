package top.zenyoung.security.spi.token;

import top.zenyoung.security.spi.exception.TokenException;

import javax.annotation.Nonnull;

/**
 * 令牌-服务接口
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 7:59 下午
 **/
public interface TokenService {
    /**
     * 创建令牌票据
     *
     * @param data 登录数据
     * @return 令牌票据
     */
    TokenTicket createToken(@Nonnull final TokenDetail data);

    /**
     * 解析登录令牌
     *
     * @param token 登录令牌
     * @return 登录数据
     * @throws TokenException 令牌异常
     */
    TokenDetail parseToken(@Nonnull final String token) throws TokenException;

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 登录令牌
     * @throws TokenException 令牌异常
     */
    String refreshToken(@Nonnull final String refreshToken) throws TokenException;

    /**
     * 清空刷新令牌
     *
     * @param refreshToken 刷新令牌
     */
    void cleanRefreshToken(@Nonnull final String refreshToken);
}
