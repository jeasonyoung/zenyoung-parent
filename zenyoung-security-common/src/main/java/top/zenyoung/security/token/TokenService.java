package top.zenyoung.security.token;

import top.zenyoung.security.exception.TokenException;

import javax.annotation.Nonnull;

/**
 * 令牌服务接口
 *
 * @author young
 */
public interface TokenService {
    /**
     * 创建令牌
     *
     * @param ticket 用户票据信息
     * @return 令牌数据
     */
    Token createToken(@Nonnull final Ticket ticket);

    /**
     * 解析令牌
     *
     * @param token 令牌串
     * @return 用户票据信息
     * @throws TokenException 令牌异常
     */
    Ticket parseToke(@Nonnull final String token) throws TokenException;

    /**
     * 验证令牌
     *
     * @param token 令牌串
     * @return 用户票据信息
     */
    Ticket valid(@Nonnull final String token);

    /**
     * 获取刷新令牌
     *
     * @param token 令牌串
     * @return 刷新令牌串
     */
    String getRefreshToken(@Nonnull final String token);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 刷新令牌
     */
    Token refreshToken(@Nonnull final String refreshToken);
}
