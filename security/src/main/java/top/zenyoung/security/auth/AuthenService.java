package top.zenyoung.security.auth;

import top.zenyoung.security.dto.AuthUserDTO;
import top.zenyoung.security.dto.UserInfoDTO;

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
    AuthUserDTO findByAccount(@Nonnull final String account);

    /**
     * 加载用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(@Nonnull final Long userId);
}
