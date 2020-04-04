package top.zenyoung.security.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.*;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.security.spi.Constants;
import top.zenyoung.security.spi.exception.TokenException;
import top.zenyoung.security.spi.exception.TokenExpireException;
import top.zenyoung.security.spi.token.TokenService;
import top.zenyoung.controller.util.RespUtils;

import javax.annotation.Nonnull;

/**
 * JWT-令牌认证-拦截器
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/1/7 11:00 下午
 **/
@Slf4j
public class JwtTokenWebFilter implements WebFilter, AuthFilter {
    private final ServerAuthenticationConverter authenticationConverter;
    private final ServerAuthenticationFailureHandler authenticationFailureHandler;

    private final ServerWebExchangeMatcher requiresAuthenticationMatcher;

    private ServerSecurityContextRepository securityContextRepository = NoOpServerSecurityContextRepository.getInstance();
    private ServerAuthenticationSuccessHandler authenticationSuccessHandler = new WebFilterChainServerAuthenticationSuccessHandler();

    /**
     * 构造函数
     *
     * @param tokenService 令牌服务
     * @param objectMapper Json处理器
     */
    public JwtTokenWebFilter(@Nonnull final TokenService tokenService, @Nonnull final ObjectMapper objectMapper) {
        //令牌认证处理
        this.authenticationConverter = new TokenAuthenticationConverter(tokenService);
        //登录地址
        this.requiresAuthenticationMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, getAuthLoginUrls());
        //令牌认证失败处理
        this.authenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler((exchange, e) -> {
            //令牌过期
            if (e instanceof TokenExpireException) {
                return RespUtils.buildResponse(exchange.getResponse(), objectMapper, Constants.LOGIN_TOKEN_EXPIRE, e);
            }
            //令牌无效
            if (e instanceof TokenException) {
                return RespUtils.buildResponse(exchange.getResponse(), objectMapper, Constants.LOGIN_TOKEN_INVALID, e);
            }
            //令牌异常
            return RespUtils.buildResponse(exchange.getResponse(), objectMapper, Constants.ACCESS_DENIED, e);
        });
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        return requiresAuthenticationMatcher.matches(exchange)
                .flatMap(matchResult -> authenticationConverter.convert(exchange))
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .flatMap(authen -> authenticate(exchange, chain, authen))
                .onErrorResume(AuthenticationException.class, e -> authenticationFailureHandler.onAuthenticationFailure(new WebFilterExchange(exchange, chain), e));
    }

    private Mono<Void> authenticate(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain, @Nonnull final Authentication authen) {
        final SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authen);
        return securityContextRepository.save(exchange, securityContext)
                .then(authenticationSuccessHandler.onAuthenticationSuccess(new WebFilterExchange(exchange, chain), authen))
                .subscriberContext(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }
}
