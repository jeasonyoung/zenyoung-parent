package top.zenyoung.framework.auth;

import javax.annotation.Nonnull;

/**
 * 认证服务接口
 *
 * @author young
 */
public interface AuthenService {
    /**
     * 查找登录用户数据
     *
     * @param account 用户账号
     * @return 认证用户数据
     */
    AuthUser findByAccount(@Nonnull final String account);

    /**
     * 加载用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfo getUserInfo(@Nonnull final Long userId);

    /**
     * 创建令牌
     *
     * @param info 用户信息
     * @return 用户令牌
     */
    AuthToken create(@Nonnull final UserInfo info);

    /**
     * 验证令牌
     *
     * @param token 认证令牌
     * @return 用户信息
     */
    UserInfo verify(@Nonnull final String token);

    /**
     * 刷新令牌
     *
     * @param refershToken 刷新令牌串
     * @return 用户令牌数据
     */
    AuthToken refersh(@Nonnull final String refershToken);

    /**
     * 令牌登出
     *
     * @param token 令牌
     */
    void logout(@Nonnull final String token);
}
