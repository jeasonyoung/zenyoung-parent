package top.zenyoung.framework.runtime.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.framework.auth.AuthUser;
import top.zenyoung.framework.auth.AuthenService;
import top.zenyoung.framework.auth.BaseAuthenticationManagerService;
import top.zenyoung.framework.auth.UserInfo;
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.model.TokenUserDetails;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.Token;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.token.TokenVerifyService;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * 认证管理器服务实现
 *
 * @author young
 */
@Service
@RequiredArgsConstructor
public class AuthenticationManagerServiceImpl extends BaseAuthenticationManagerService {
    private final AuthenService authenService;
    private final TokenService tokenService;
    private final TokenVerifyService tokenVerifyService;

    @Override
    protected Ticket parseToken(@Nonnull final String token) {
        return tokenVerifyService.checkToken(token);
    }

    @Override
    protected UserDetails retrieveUser(@Nonnull final String username) throws AuthenticationException {
        final AuthUser user = authenService.findByAccount(username);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        final UserPrincipal principal = new UserPrincipal(String.valueOf(user.getId()), user.getAccount(), user.getRoles(), null);
        return TokenUserDetails.of(principal, user.getPassword(), user.getStatus());
    }

    @Nonnull
    @Override
    protected LoginRespBody buildSuccessfulLoginBody(@Nonnull final UserPrincipal principal) {
        final Token token = tokenService.createToken(new Ticket(principal));
        final UserInfo userInfo = authenService.getUserInfo(Long.parseLong(principal.getId()));
        return LoginRespBody.builder()
                .token(token.getAccessToken())
                .refreshToken(token.getRefershToken())
                .user(new LinkedHashMap<String, Serializable>() {
                    {
                        if (userInfo != null) {
                            put("name", userInfo.getName());
                            put("nick", userInfo.getNick());
                            put("avatar", userInfo.getAvatar());
                            put("roles", Lists.newLinkedList(userInfo.getRoles()));
                        } else {
                            put("roles", Lists.newLinkedList(principal.getRoles()));
                        }
                    }
                })
                .build();
    }
}
