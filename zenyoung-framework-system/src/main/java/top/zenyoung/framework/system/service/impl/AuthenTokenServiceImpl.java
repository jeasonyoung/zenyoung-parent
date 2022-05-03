package top.zenyoung.framework.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.zenyoung.framework.auth.AuthToken;
import top.zenyoung.framework.auth.AuthUser;
import top.zenyoung.framework.auth.UserInfo;
import top.zenyoung.framework.system.dao.repository.UserRepository;
import top.zenyoung.framework.system.service.AuthenTokenService;

import javax.annotation.Nonnull;

/**
 * 认证令牌服务接口实现
 *
 * @author young
 */
@Service
@RequiredArgsConstructor
public class AuthenTokenServiceImpl implements AuthenTokenService {
    private final UserRepository repository;

    @Override
    public AuthUser findByAccount(@Nonnull final String account) {
        return null;
    }

    @Override
    public UserInfo getUserInfo(@Nonnull final Long userId) {
        return null;
    }

    @Override
    public AuthToken create(@Nonnull final UserInfo info) {
        return null;
    }

    @Override
    public UserInfo verify(@Nonnull final String token) {
        return null;
    }

    @Override
    public AuthToken refersh(@Nonnull final String refershToken) {
        return null;
    }

    @Override
    public void logout(@Nonnull final String token) {

    }
}
