package top.zenyoung.security;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
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
import java.lang.reflect.InvocationTargetException;

/**
 * 令牌认证管理器-基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseJwtAuthenticationManager<ReqBody extends LoginReqBody> {
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
    public abstract Class<ReqBody> getLoginReqBodyClass();

    /**
     * 创建请求新对象
     *
     * @return 请求新对象
     */
    @SneakyThrows({NoSuchMethodException.class, InstantiationException.class, IllegalAccessException.class, IllegalArgumentException.class, InvocationTargetException.class})
    public ReqBody createReqBody() {
        return getLoginReqBodyClass().getDeclaredConstructor().newInstance();
    }

    /**
     * 解析用户认证令牌
     *
     * @param token 令牌数据
     * @return 用户数据
     * @throws TokenException 令牌异常
     */
    protected TokenAuthentication<ReqBody> parseAuthenticationToken(@Nullable final String token) throws TokenException {
        log.debug("parseAuthenticationToken(token: {})...", token);
        if (!Strings.isNullOrEmpty(token)) {
            String tokenVal = token.trim();
            //检查是否有Bearer
            final String bearer = "Bearer ";
            if (token.startsWith(bearer)) {
                tokenVal = token.replaceFirst(bearer, "").trim();
            }
            final Ticket ticket = getTokenGenerator().parseToken(tokenVal);
            if (ticket == null) {
                throw new TokenException("令牌无效!");
            }
            final TokenUserDetails userDetails = new TokenUserDetails(ticket);
            return new TokenAuthentication<>(userDetails);
        }
        return null;
    }

    /**
     * 认证前校验
     *
     * @param reqBody 登录请求数据
     * @throws AuthenticationException 认证异常
     */
    protected void preAuthenticationChecked(@Nonnull final ReqBody reqBody) throws AuthenticationException {
        log.debug("preAuthChecked(reqBody: {})...", reqBody);
    }

    /**
     * 用户认证处理
     *
     * @param reqBody 用户数据
     * @return 用户数据
     */
    protected abstract TokenUserDetails userAuthenHandler(@Nonnull final ReqBody reqBody);
}
