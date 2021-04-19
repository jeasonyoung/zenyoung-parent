package top.zenyoung.security.webflux;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.LoginRespBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.token.JwtTokenGenerator;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.TokenGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 认证管理器接口
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/20 5:59 下午
 **/
public interface ZyAuthenticationManager extends ReactiveAuthenticationManager {

    /**
     * 获取令牌
     *
     * @return 令牌
     */
    @Nonnull
    default TokenGenerator getTokenGenerator() {
        return new JwtTokenGenerator();
    }

    /**
     * 获取登录请求方法
     *
     * @return 登录请求方法
     */
    @Nonnull
    default HttpMethod getLoginMethod() {
        return HttpMethod.POST;
    }

    /**
     * 获取登录请求地址集合
     *
     * @return 登录请求地址集合
     */
    @Nonnull
    default String[] getLoginUrls() {
        return new String[0];
    }

    /**
     * 获取白名单Urls.
     *
     * @return 白名单Urls.
     */
    @Nullable
    default String[] getWhiteUrls() {
        return null;
    }

    /**
     * 获取密码编码器
     *
     * @return 密码编码器
     */
    default PasswordEncoder getPasswordEncoder() {
        return null;
    }

    /**
     * 构建用户认证服务实现
     *
     * @param reqBody 请求数据
     * @param path    请求路径
     * @return 认证服务实现
     */
    default ReactiveUserDetailsService buildAuthService(@Nonnull final LoginReqBody reqBody, @Nonnull final RequestPath path) {
        return null;
    }

    /**
     * 获取用户登录请求报文类型
     *
     * @return 用户登录请求报文类型
     */
    default Class<? extends LoginReqBody> getLoginReqBodyClass() {
        return LoginReqBody.class;
    }

    /**
     * 构建响应数据
     *
     * @param principal 用户信息
     * @return 响应数据
     */
    @Nonnull
    default LoginRespBody createRespBody(@Nonnull final UserPrincipal principal) {
        //返回数据
        return new LoginRespBody();
    }

    /**
     * 构建登录用户数据
     *
     * @param respBody  响应数据
     * @param principal 用户数据
     */
    default void buildRespBody(@Nonnull final LoginRespBody respBody, @Nonnull final UserPrincipal principal) {
        //登录令牌
        respBody.setToken(getTokenGenerator().createToken(new Ticket(principal)));
    }

    /**
     * 解析令牌
     *
     * @param token 令牌串
     * @return 用户票据数据
     * @throws TokenException 票据认证异常
     */
    default Ticket parseToken(@Nonnull final String token) throws TokenException {
        return getTokenGenerator().parseToken(token);
    }

    /**
     * 认证业务处理
     *
     * @param authentication 认证数据
     * @return 认证结果
     */
    @Override
    default Mono<Authentication> authenticate(final Authentication authentication) {
        Assert.notNull(authentication, "'authentication'不能为空!");
        if (authentication instanceof TokenAuthentication) {
            final TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
            //初始化认证处理器
            final UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(
                    buildAuthService(tokenAuthentication.getReqBody(), tokenAuthentication.getPath())
            );
            //设置密码编码器
            manager.setPasswordEncoder(getPasswordEncoder());
            //认证处理
            return manager.authenticate(tokenAuthentication);
        }
        return Mono.error(new IllegalArgumentException("authentication is TokenAuthentication"));
    }
}