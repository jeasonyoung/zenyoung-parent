package top.zenyoung.security.token;

import javax.annotation.Nonnull;

/**
 * 令牌校验服务
 *
 * @author young
 */
public interface TokenVerifyService {

    /**
     * 校验令牌串
     *
     * @param token 令牌串
     * @return 用户信息
     */
    Ticket checkToken(@Nonnull final String token);

    /**
     * 校验刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 用户信息
     */
    Ticket checkRefreshToken(@Nonnull final String refreshToken);
}
