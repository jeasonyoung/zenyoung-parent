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
     * 刷新令牌串
     *
     * @param refreshToken 刷新令牌串
     * @return 新令牌串
     */
    String refreshToken(@Nonnull final String refreshToken);

    /**
     * 解析令牌
     *
     * @param token 令牌串
     * @return 令牌票据
     * @throws TokenException 令牌异常
     */
    Ticket parseToken(@Nonnull final String token) throws TokenException;

    /**
     * 解析刷新令牌
     *
     * @param refreshToken 刷新令牌串
     * @return 令牌票据
     */
    Ticket parseRefreshToken(@Nonnull final String refreshToken);

    /**
     * 验证令牌
     *
     * @param token 令牌串
     * @return 令牌票据
     */
    Ticket validToken(@Nonnull final String token);

    /**
     * 删除令牌
     *
     * @param token 令牌串
     */
    void delToken(@Nonnull final String token);

    /**
     * 根据令牌获取刷新令牌
     *
     * @param token 令牌串
     * @return 刷新令牌串
     */
    String getRefreshToken(@Nonnull final String token);

    /**
     * 根据刷新令牌获取令牌
     *
     * @param refreshToken 刷新令牌
     * @return 令牌
     */
    String getToken(@Nonnull final String refreshToken);
}
