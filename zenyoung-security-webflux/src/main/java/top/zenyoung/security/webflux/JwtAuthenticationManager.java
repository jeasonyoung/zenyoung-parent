package top.zenyoung.security.webflux;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.security.BaseJwtAuthenticationManager;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.model.TokenUserDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 认证管理器接口
 *
 * @author young
 */
@Slf4j
public abstract class JwtAuthenticationManager extends BaseJwtAuthenticationManager implements ReactiveAuthenticationManager {

    /**
     * 获取服务端编码配置
     *
     * @return 服务端编码配置
     */
    @Nonnull
    public abstract ServerCodecConfigurer getServerCodecConfigurer();

    /**
     * 获取表单提交-用户账号参数字段
     *
     * @return 用户账号参数字段
     */
    @Nonnull
    protected String getUsernameParameter() {
        return "username";
    }

    /**
     * 获取表单提交-用户密码参数字段
     *
     * @return 用户密码参数字段
     */
    @Nonnull
    protected String getPasswordParameter() {
        return "password";
    }

    /**
     * 解析表单提交认证
     *
     * @param formData 表达数据
     * @return 认证数据
     */
    public Mono<TokenAuthentication> parseFromData(@Nonnull final Mono<MultiValueMap<String, String>> formData) {
        return formData.map(data -> {
            final String username = data.getFirst(getUsernameParameter());
            final String password = data.getFirst(getPasswordParameter());
            final LoginReqBody reqBody = new LoginReqBody();
            reqBody.setAccount(username);
            reqBody.setPasswd(password);
            return new TokenAuthentication(reqBody);
        });
    }

    /**
     * 解析用户认证令牌
     *
     * @param request 请求对象
     * @return 认证令牌
     */
    public TokenAuthentication parseAuthenticationToken(@Nonnull final ServerHttpRequest request) {
        final String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!Strings.isNullOrEmpty(token)) {
            return parseAuthenticationToken(token);
        }
        return null;
    }

    @Override
    public Mono<Authentication> authenticate(final Authentication authentication) {
        log.debug("authenticate(authentication: {})...", authentication);
        //认证数据
        TokenAuthentication tokenAuthen = null;
        if (authentication instanceof TokenAuthentication) {
            tokenAuthen = (TokenAuthentication) authentication;
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            tokenAuthen = new TokenAuthentication((UsernamePasswordAuthenticationToken) authentication);
        }
        //检查认证数据
        if (tokenAuthen == null) {
            return Mono.error(new BadCredentialsException("获取认证数据失败!=>" + authentication));
        }
        //初始化认证处理器
        final UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(
                buildAuthService(tokenAuthen.getReqBody())
        );
        //设置密码编码器
        manager.setPasswordEncoder(getPasswordEncoder());
        //认证处理
        return manager.authenticate(tokenAuthen);
    }

    /**
     * 构建用户认证服务实现
     *
     * @param reqBody 请求数据
     * @return 认证服务实现
     */
    protected ReactiveUserDetailsService buildAuthService(@Nonnull final LoginReqBody reqBody) {
        log.debug("buildAuthService(reqBody: {})...", reqBody);
        return username -> {
            try {
                //认证前校验
                preAuthenticationChecked(reqBody);
                //调用用户认证
                final TokenUserDetails userDetails = userAuthenHandler(reqBody.getAccount());
                if (userDetails == null) {
                    return Mono.error(new UsernameNotFoundException("用户名不存在!"));
                }
                return Mono.just(userDetails);
            } catch (AuthenticationException ex) {
                log.warn("buildUserDetailsService(reqBody: {})-exp: {}", reqBody, ex.getMessage());
                return Mono.error(ex);
            } catch (Throwable ex) {
                log.warn("buildUserDetailsService(reqBody: {})-exp: {}", reqBody, ex.getMessage());
                return Mono.error(new AuthenticationException(ex.getMessage(), ex) {
                });
            }
        };
    }

    /**
     * 认证成功处理
     *
     * @param response  响应数据流
     * @param principal 登录成功用户信息
     * @return 处理结果
     */
    public abstract Mono<Void> successfulAuthenticationHandler(@Nonnull final ServerHttpResponse response, @Nonnull final UserPrincipal principal);

    /**
     * 认证失败处理
     *
     * @param response 响应数据流
     * @param failed   认证异常
     * @return 处理结果
     */
    public abstract Mono<Void> unsuccessfulAuthentication(@Nonnull final ServerHttpResponse response, @Nullable final AuthenticationException failed);
}
