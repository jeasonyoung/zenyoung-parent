package top.zenyoung.security.webflux.filter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.security.model.LoginReqBody;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.webflux.JwtAuthenticationManager;
import top.zenyoung.security.webflux.TopSecurityContext;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jwt令牌认证-过滤器
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 3:55 下午
 **/
@Slf4j
public class JwtTokenFilter implements WebFilter {
    private final JwtAuthenticationManager<? extends LoginReqBody> manager;
    private final ServerWebExchangeMatcher whiteMatchers;

    private final ServerSecurityContextRepository securityContextRepository;

    private final ServerAuthenticationSuccessHandler authenticationSuccessHandler;
    private final ServerAuthenticationFailureHandler authenticationFailureHandler;

    public JwtTokenFilter(@Nonnull final JwtAuthenticationManager<? extends LoginReqBody> manager) {
        this.securityContextRepository = NoOpServerSecurityContextRepository.getInstance();

        this.manager = manager;
        this.whiteMatchers = buildExchangeMatchers();
        this.authenticationSuccessHandler = new WebFilterChainServerAuthenticationSuccessHandler();
        this.authenticationFailureHandler = new ServerAuthenticationEntryPointFailureHandler(
                (exchange, ex) -> manager.unsuccessfulAuthentication(exchange.getResponse(), ex)
        );
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        return ServerWebExchangeMatchers.anyExchange().matches(exchange)
                .flatMap(ret -> {
                    final ServerHttpRequest request = exchange.getRequest();
                    //白名单处理
                    if (whiteMatchers != null) {
                        return whiteMatchers.matches(exchange)
                                .filter(matchResult -> !matchResult.isMatch())
                                .map(matchResult -> manager.parseAuthenticationToken(request))
                                .switchIfEmpty(Mono.empty())
                                .onErrorResume(ex -> fallback(request, ex));
                    }
                    return Mono.just(manager.parseAuthenticationToken(request))
                            .onErrorResume(ex -> fallback(request, ex));
                })
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .flatMap(authen -> authenticate(exchange, chain, authen))
                .onErrorResume(AuthenticationException.class, e -> authenticationFailureHandler.onAuthenticationFailure(new WebFilterExchange(exchange, chain), e));
    }

    private Mono<Void> authenticate(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain, @Nonnull final Authentication authen) {
        final SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authen);
        SecurityContextHolder.setContext(new TopSecurityContext(securityContext, exchange.getRequest()));
        return this.securityContextRepository.save(exchange, securityContext)
                .then(authenticationSuccessHandler.onAuthenticationSuccess(new WebFilterExchange(exchange, chain), authen))
                .contextWrite(context -> ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
    }

    private ServerWebExchangeMatcher buildExchangeMatchers() {
        final List<String> whiteUrls = Lists.newLinkedList();
        //用户登录
        final String[] loginUrls = manager.getLoginUrls();
        if (loginUrls.length > 0) {
            whiteUrls.addAll(Arrays.stream(loginUrls)
                    .filter(val -> !Strings.isNullOrEmpty(val))
                    .collect(Collectors.toList())
            );
        }
        //白名单
        final String[] urls = manager.getWhiteUrls();
        if (urls != null && urls.length > 0) {
            whiteUrls.addAll(Arrays.stream(urls)
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .collect(Collectors.toList())
            );
        }
        if (!CollectionUtils.isEmpty(whiteUrls)) {
            return ServerWebExchangeMatchers.pathMatchers(whiteUrls.toArray(new String[0]));
        }
        return null;
    }

    protected <R extends LoginReqBody> Mono<TokenAuthentication<R>> fallback(@Nonnull final ServerHttpRequest request, @Nonnull final Throwable ex) {
        log.debug("fallback(request-path: {})-exp: {}", request.getPath(), ex.getMessage());
        if (ex instanceof AuthenticationException) {
            return Mono.error(ex);
        }
        return Mono.error(new AuthenticationException(ex.getMessage(), ex) {
        });
    }
}
