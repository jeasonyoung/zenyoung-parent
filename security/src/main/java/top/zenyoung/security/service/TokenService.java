package top.zenyoung.security.service;

import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.Ticket;
import top.zenyoung.security.model.Token;

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
     * @param accessToken 访问令牌串
     * @return 令牌票据
     * @throws TokenException 令牌异常
     */
    Ticket parseToken(@Nonnull final String accessToken) throws TokenException;

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
     * @param accessToken 令牌串
     * @return 令牌票据
     */
    Ticket validToken(@Nonnull final String accessToken);

    /**
     * 删除令访令牌
     *
     * @param accessToken 令访令牌串
     */
    void delToken(@Nonnull final String accessToken);

    /**
     * 根据访问令牌获取刷新令牌
     *
     * @param accessToken 令访问牌串
     * @return 刷新令牌串
     */
    String getRefreshToken(@Nonnull final String accessToken);

    /**
     * 根据刷新令牌获取令牌
     *
     * @param refreshToken 刷新令牌
     * @return 令牌
     */
    String getToken(@Nonnull final String refreshToken);
}
