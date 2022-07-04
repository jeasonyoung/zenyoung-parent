package top.zenyoung.security.auth;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.zenyoung.security.dto.LoginBodyDTO;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.Ticket;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.model.TokenUserDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 令牌认证管理器-基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseRestfulAuthenticationManager<ReqBody extends LoginBodyDTO> implements AuthenticationManager {
    protected final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    protected final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    protected final UserDetailsChecker preAuthenticationChecks = user -> {
        if (!user.isAccountNonLocked()) {
            log.debug("Failed to authenticate since user account is locked");
            throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
        } else if (!user.isEnabled()) {
            log.debug("Failed to authenticate since user account is disabled");
            throw new DisabledException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
        } else if (!user.isAccountNonExpired()) {
            log.debug("Failed to authenticate since user account has expired");
            throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
        }
    };
    protected final UserDetailsChecker postAuthenticationChecks = user -> {
        if (!user.isCredentialsNonExpired()) {
            log.debug("Failed to authenticate since user account credentials have expired");
            throw new CredentialsExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired", "User credentials have expired"));
        }
    };

    /**
     * 获取白名单Urls.
     *
     * @return 白名单Urls.
     */
    public String[] getWhiteUrls() {
        return null;
    }

    /**
     * 获取登录请求地址集合
     *
     * @return 登录请求地址集合
     */
    @Nonnull
    public abstract String[] getLoginUrls();

    /**
     * 获取用户登录请求报文类型
     *
     * @return 用户登录请求报文类型
     */
    @Nonnull
    public abstract Class<ReqBody> getLoginReqBodyClass();

    /**
     * 获取密码编辑器
     *
     * @return 密码编辑器
     */
    @Nonnull
    protected abstract PasswordEncoder getPasswordEncoder();

    /**
     * 解析用户认证令牌
     *
     * @param token 令牌数据
     * @return 用户数据
     * @throws TokenException 令牌异常
     */
    protected TokenAuthentication<ReqBody> parseAuthenticationToken(@Nullable final String token) throws TokenException {
        if (!Strings.isNullOrEmpty(token)) {
            String tokenVal = token.trim();
            //检查是否有Bearer
            final String bearer = "Bearer ";
            if (token.startsWith(bearer)) {
                tokenVal = token.replaceFirst(bearer, "").trim();
            }
            final Ticket ticket = parseToken(tokenVal);
            if (ticket == null) {
                throw new TokenException("令牌无效!");
            }
            final TokenUserDetails userDetails = new TokenUserDetails(ticket);
            return new TokenAuthentication<>(userDetails);
        }
        return null;
    }

    /**
     * 解析令牌串
     *
     * @param token 令牌串
     * @return 用户信息
     */
    protected abstract Ticket parseToken(@Nonnull final String token);

    @Override
    public Authentication authenticate(final Authentication authen) throws AuthenticationException {
        final String username = determineUsername(authen);
        final UserDetails user = retrieveUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名[" + username + "]不存在!");
        }
        //检查用户信息
        this.preAuthenticationChecks.check(user);
        //验证认证
        this.authenticationChecks(user, authen);
        this.postAuthenticationChecks.check(user);
        //创建认证成功
        return this.createSuccessAuthentication(user, authen);
    }

    /**
     * 获取用户名
     *
     * @param authen 认证数据
     * @return 用户名
     */
    private String determineUsername(final Authentication authen) {
        return authen.getPrincipal() == null ? "NONE_PROVIDED" : authen.getName();
    }

    /**
     * 获取验证用户信息
     *
     * @param username 用户名
     * @return 用户信息
     * @throws AuthenticationException 认证异常
     */
    protected abstract UserDetails retrieveUser(@Nonnull final String username) throws AuthenticationException;

    /**
     * 身份验证检查
     *
     * @param userDetails 用户信息数据
     * @param authen      认证数据
     * @throws AuthenticationException 认证异常
     */
    protected void authenticationChecks(final UserDetails userDetails, final Authentication authen) throws AuthenticationException {
        if (authen instanceof TokenAuthentication) {
            //检查是否免密
            if (((TokenAuthentication<?>) authen).getFreePwdAuthen()) {
                return;
            }
        }
        //验证密码
        if (authen.getCredentials() == null) {
            log.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        final String pwd = authen.getCredentials().toString();
        final PasswordEncoder encoder = getPasswordEncoder();
        if (!encoder.matches(pwd, userDetails.getPassword())) {
            log.debug("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    /**
     * 创建成功验证
     *
     * @param user   用户信息
     * @param authen 认证数据
     * @return 验证成功
     */
    protected Authentication createSuccessAuthentication(final UserDetails user, final Authentication authen) {
        final UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                user, authen.getCredentials(), this.authoritiesMapper.mapAuthorities(user.getAuthorities())
        );
        result.setDetails(authen.getDetails());
        log.debug("Authenticated user");
        return result;
    }
}
