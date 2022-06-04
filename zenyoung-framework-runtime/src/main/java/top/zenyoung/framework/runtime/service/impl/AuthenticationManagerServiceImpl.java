package top.zenyoung.framework.runtime.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.framework.auth.AuthUser;
import top.zenyoung.framework.auth.AuthenService;
import top.zenyoung.framework.auth.BaseAuthenticationManagerService;
import top.zenyoung.framework.utils.BeanCacheUtils;
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.model.TokenUserDetails;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.Token;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.token.TokenVerifyService;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 认证管理器服务实现
 *
 * @author young
 */
@RequiredArgsConstructor
public class AuthenticationManagerServiceImpl extends BaseAuthenticationManagerService {
    private final ApplicationContext context;

    @Override
    protected Ticket parseToken(@Nonnull final String token) {
        return BeanCacheUtils.function(context, TokenVerifyService.class, bean -> bean.checkToken(token));
    }

    @Override
    protected UserDetails retrieveUser(@Nonnull final String username) throws AuthenticationException {
        final AuthUser user = BeanCacheUtils.function(context, AuthenService.class, bean -> bean.findByAccount(username));
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        final UserPrincipal principal = new UserPrincipal(String.valueOf(user.getId()), user.getAccount(), user.getRoles(), null);
        return TokenUserDetails.of(principal, user.getPassword(), user.getStatus());
    }

    @Nonnull
    @Override
    protected LoginRespBody buildSuccessfulLoginBody(@Nonnull final UserPrincipal principal) {
        final Token token = BeanCacheUtils.function(context, TokenService.class, bean -> bean.createToken(new Ticket(principal)));
        Assert.notNull(token, "生成令牌失败!");
        return LoginRespBody.builder()
                .token(token.getAccessToken())
                .refreshToken(token.getRefershToken())
                .user(new LinkedHashMap<String, Serializable>() {
                    {
                        put("roles", Lists.newLinkedList(principal.getRoles()));
                        final Map<String, Serializable> exts = principal.getExts();
                        if (!CollectionUtils.isEmpty(exts)) {
                            putAll(exts);
                        }
                    }
                })
                .build();
    }
}
