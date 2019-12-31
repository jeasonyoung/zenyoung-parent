package top.zenyoung.security.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import top.zenyoung.security.spi.TokenUserDetail;
import top.zenyoung.security.spi.auth.*;
import top.zenyoung.security.spi.token.TokenAuthentication;
import top.zenyoung.security.spi.token.TokenDetail;
import top.zenyoung.security.spi.token.TokenService;
import top.zenyoung.security.spi.token.TokenTicket;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 用户认证管理器基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/12/22 8:20 下午
 **/
@Slf4j
public abstract class BaseAuthenticationManager implements AuthenticationManager {
    /**
     * 密码编辑器
     */
    private PasswordEncoder passwordEncoder = null;

    /**
     * 设置密码编辑器
     *
     * @param passwordEncoder 密码编辑器
     */
    public void setPasswordEncoder(@Nonnull final PasswordEncoder passwordEncoder) {
        Assert.notNull(passwordEncoder, "'passwordEncoder'不能为空!");
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<Authentication> authenticate(final Authentication authentication) {
        if (authentication instanceof TokenAuthentication) {
            return authenticate((TokenAuthentication) authentication);
        } else {
            return Mono.error(new IllegalArgumentException("authentication is not TokenAuthentication"));
        }
    }

    private Mono<Authentication> authenticate(final TokenAuthentication token) {
        final UserDetailService userDetailService = getUserDetailService(token.getType());
        if (userDetailService == null) {
            return Mono.error(new UnsupportedOperationException("用户[" + token.getType() + "]未实现 UserDetailService接口"));
        }
        final UserDetailsRepositoryAuthenticationManager manager = new UserDetailsRepositoryAuthenticationManager(userDetailService);
        if (this.passwordEncoder != null) {
            manager.setPasswordEncoder(this.passwordEncoder);
        }
        return manager.authenticate(token);
    }

    /**
     * 根据类型获取用户认证服务
     *
     * @param userType 用户类型
     * @return 用户认证服务
     */
    protected abstract UserDetailService getUserDetailService(@Nonnull final Integer userType);

    /**
     * 获取令牌服务接口
     *
     * @return 令牌服务接口
     */
    protected abstract TokenService getTokenService();

    /**
     * 获取用户认证响应数据
     *
     * @param tokenDetail 令牌用户数据
     * @return 用户认证响应数据
     */
    @Override
    public RespLoginBody getUserResp(@Nonnull final TokenDetail tokenDetail) {
        Assert.notNull(tokenDetail.getType(), "'tokenDetail.getType()'不能为空!");
        Assert.hasText(tokenDetail.getUserId(), "'tokenDetail.getUserId()'不能为空!");
        final UserDetailService service = getUserDetailService(tokenDetail.getType());
        if (service == null) {
            throw new UnsupportedOperationException("用户[" + tokenDetail.getType() + "]未实现 UserDetailService接口");
        }
        //用户信息
        final UserInfo userInfo = service.getUserInfoById(tokenDetail.getUserId());
        if (userInfo == null) {
            throw new RuntimeException("用户[" + tokenDetail.getUserId() + "]信息不存在!");
        }
        //获取用户菜单集合
        final List<Menu> menus = service.getMenusByUserId(tokenDetail.getUserId());
        //检查令牌服务
        final TokenService tokenService = getTokenService();
        Assert.notNull(tokenService, "'getTokenService()'不能为空!");
        //创建令牌
        final TokenTicket ticket = tokenService.createToken(tokenDetail);
        //构建响应报文体
        return RespLoginBody.builder()
                //登录令牌
                .token(ticket.getToken())
                //刷新令牌
                .refreshToken(ticket.getRefreshToken())
                //用户信息
                .user(new RespLoginBody.User(userInfo, tokenDetail.getRoles()))
                //用户菜单集合
                .menus(CollectionUtils.isEmpty(menus) ?  null : menus)
                .build();
    }

    private static class UserDetailsRepositoryAuthenticationManager extends UserDetailsRepositoryReactiveAuthenticationManager {

        private UserDetailsRepositoryAuthenticationManager(@Nonnull final UserDetailService userDetailService) {
            super(new UserDetailsServiceImpl(userDetailService));
        }
    }

    private static class UserDetailsServiceImpl implements ReactiveUserDetailsService {
        private final UserDetailService service;

        UserDetailsServiceImpl(final UserDetailService service) {
            this.service = service;
        }

        private UserDetails getUserByAccount(final String account) {
            final UserDetail data = service.getAuthenByAccount(account);
            if (data == null) {
                return null;
            }
            return new TokenUserDetail(data);
        }

        @Override
        public Mono<UserDetails> findByUsername(final String username) {
            return Mono.justOrEmpty(getUserByAccount(username));
        }
    }
}
