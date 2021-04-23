package top.zenyoung.security;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.model.TokenUserDetails;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.TokenGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 令牌认证管理器-基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseJwtAuthenticationManager {
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
     * 获取密码编码器
     *
     * @return 密码编码器
     */
    @Nonnull
    protected abstract PasswordEncoder getPasswordEncoder();

    /**
     * 获取令牌生成器
     *
     * @return 令牌生成器
     */
    @Nonnull
    protected abstract TokenGenerator getTokenGenerator();

    /**
     * 获取用户登录请求报文类型
     *
     * @return 用户登录请求报文类型
     */
    @Nonnull
    public Class<? extends LoginReqBody> getLoginReqBodyClass() {
        return LoginReqBody.class;
    }

    /**
     * 解析用户认证令牌
     *
     * @param token 令牌数据
     * @return 用户数据
     * @throws TokenException 令牌异常
     */
    protected TokenAuthentication parseAuthenticationToken(@Nullable final String token) throws TokenException {
        log.debug("parseAuthenticationToken(token: {})...", token);
        if (!Strings.isNullOrEmpty(token)) {
            final Ticket ticket = getTokenGenerator().parseToken(token);
            if (ticket == null) {
                throw new TokenException("令牌无效!");
            }
            final TokenUserDetails userDetails = new TokenUserDetails(ticket);
            return new TokenAuthentication(userDetails);
        }
        return null;
    }

    /**
     * 认证前校验
     *
     * @param reqBody   登录请求数据
     * @param <ReqBody> 请求数据类型
     * @throws AuthenticationException 认证异常
     */
    protected <ReqBody extends LoginReqBody> void preAuthenticationChecked(@Nonnull final ReqBody reqBody) throws AuthenticationException {
        log.debug("preAuthChecked(reqBody: {})...", reqBody);
    }

    /**
     * 用户认证处理
     *
     * @param username 用户账号
     * @return 用户数据
     */
    protected abstract TokenUserDetails userAuthenHandler(@Nonnull final String username);
}
