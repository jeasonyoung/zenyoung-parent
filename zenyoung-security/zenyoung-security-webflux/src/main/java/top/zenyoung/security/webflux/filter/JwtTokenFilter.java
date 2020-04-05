package top.zenyoung.security.webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import top.zenyoung.security.webflux.AuthenticationManager;
import top.zenyoung.security.webflux.converter.JwtTokenAuthenticationConverter;
import top.zenyoung.security.webflux.util.RespJsonUtils;

import javax.annotation.Nonnull;

/**
 * Jwt令牌认证-过滤器
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/3/19 3:55 下午
 **/
@Slf4j
public class JwtTokenFilter implements WebFilter {
    private final ServerSecurityContextRepository securityContextRepository;
    private final ServerAuthenticationConverter authenticationConverter;

    private ServerWebExchangeMatcher requiresAuthenticationMatcher = ServerWebExchangeMatchers.anyExchange();
    private ServerAuthenticationSuccessHandler authenticationSuccessHandler = new WebFilterChainServerAuthenticationSuccessHandler();
    private ServerAuthenticationFailureHandler authenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler(
            (exchange, e) -> RespJsonUtils.buildFailResp(exchange.getResponse(), HttpStatus.UNAUTHORIZED, e)
    );

    public void setRequiresAuthenticationMatcher(@Nonnull final ServerWebExchangeMatcher requiresAuthenticationMatcher) {
        this.requiresAuthenticationMatcher = requiresAuthenticationMatcher;
    }

    public void setAuthenticationSuccessHandler(@Nonnull ServerAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    public void setAuthenticationFailureHandler(@Nonnull ServerAuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    public JwtTokenFilter(@Nonnull final AuthenticationManager authenticationManager) {
        this.securityContextRepository = NoOpServerSecurityContextRepository.getInstance();
        this.authenticationConverter = new JwtTokenAuthenticationConverter(authenticationManager);
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        return requiresAuthenticationMatcher.matches(exchange)
                .flatMap(matchResult -> this.authenticationConverter.convert(exchange))
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .flatMap(authen -> authenticate(exchange, chain, authen))
                .onErrorResume(AuthenticationException.class, e -> authenticationFailureHandler.onAuthenticationFailure(new WebFilterExchange(exchange, chain), e));
    }

    private Mono<Void> authenticate(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain, @Nonnull final Authentication authen) {
        final SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authen);
        return this.securityContextRepository.save(exchange, securityContext)
                .then(authenticationSuccessHandler.onAuthenticationSuccess(new WebFilterExchange(exchange, chain), authen))
                .subscriberContext(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }
}
