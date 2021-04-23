package top.zenyoung.security.webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.webflux.JwtAuthenticationManager;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * Jwt登录-过滤器
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/20 5:56 下午
 **/
@Slf4j
public class JwtLoginFilter extends AuthenticationWebFilter {
    private final JwtAuthenticationManager manager;

    /**
     * 构造函数
     *
     * @param manager 认证管理器
     */
    public JwtLoginFilter(@Nonnull final JwtAuthenticationManager manager) {
        super(manager);
        this.manager = manager;
        //设置登录地址
        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, manager.getLoginUrls()));
        //登录请求参数解析
        setServerAuthenticationConverter(parseReqBody());
        //登录成功处理
        setAuthenticationSuccessHandler((filterExchange, authen) -> {
            log.debug("setAuthenticationSuccessHandler(authen: {})...", authen);
            if (authen.getPrincipal() instanceof UserPrincipal) {
                try {
                    final ServerWebExchange exchange = filterExchange.getExchange();
                    //获取登录用户数据
                    final UserPrincipal principal = new UserPrincipal((UserPrincipal) authen.getPrincipal());
                    return manager.successfulAuthenticationHandler(exchange.getResponse(), principal);
                } catch (Throwable ex) {
                    log.debug("setAuthenticationSuccessHandler(authen: {})-exp: {}", authen, ex.getMessage());
                    return Mono.error(ex);
                }
            }
            return Mono.error(new IllegalArgumentException("authen.getPrincipal()不能转换为UserPrincipal=>" + authen.getPrincipal()));
        });
        //登录失败处理
        setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(
                (exchange, e) -> manager.unsuccessfulAuthentication(exchange.getResponse(), e))
        );
    }

    protected ServerAuthenticationConverter parseReqBody() {
        return exchange -> {
            final ServerHttpRequest request = exchange.getRequest();
            final MediaType contentType = request.getHeaders().getContentType();
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                final ResolvableType reqLoginBodyType = ResolvableType.forClass(manager.getLoginReqBodyClass());
                return manager.getServerCodecConfigurer()
                        .getReaders().stream()
                        .filter(reader -> reader.canRead(reqLoginBodyType, MediaType.APPLICATION_JSON))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No JSON reader for LoginReqBody"))
                        .readMono(reqLoginBodyType, request, Collections.emptyMap())
                        .cast(LoginReqBody.class)
                        .map(TokenAuthentication::new);
            } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                return manager.parseFromData(exchange.getFormData())
                        .cast(Authentication.class);
            }
            return Mono.empty();
        };
    }

}