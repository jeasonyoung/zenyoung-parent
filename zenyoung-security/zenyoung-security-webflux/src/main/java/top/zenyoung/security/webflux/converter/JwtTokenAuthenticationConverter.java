package top.zenyoung.security.webflux.converter;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.webflux.AuthenticationManager;
import top.zenyoung.security.webflux.model.TokenAuthentication;
import top.zenyoung.security.webflux.model.TokenUserDetails;

import javax.annotation.Nonnull;

/**
 * Jwt令牌认证转换器类
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/19 4:02 下午
 **/
@Slf4j
public class JwtTokenAuthenticationConverter implements ServerAuthenticationConverter {
    private static final String TOKEN_HEADER_NAME = HttpHeaders.AUTHORIZATION;

    private final AuthenticationManager authenticationManager;
    private final ServerWebExchangeMatcher exchangeMatcher;

    public JwtTokenAuthenticationConverter(@Nonnull final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        final String[] whiteUrls = authenticationManager.getWhiteUrls();
        exchangeMatcher = whiteUrls == null || whiteUrls.length == 0 ? null : ServerWebExchangeMatchers.pathMatchers(whiteUrls);
    }

    @Override
    public Mono<Authentication> convert(@Nonnull final ServerWebExchange exchange) {
        //获取令牌
        final ServerHttpRequest request = exchange.getRequest();
        final String authorization = request.getHeaders().getFirst(TOKEN_HEADER_NAME);
        if (Strings.isNullOrEmpty(authorization)) {
            //令牌为空
            return Mono.empty();
        }
        //检查是否有白名单
        if (exchangeMatcher != null) {
            //白名单处理
            return exchangeMatcher.matches(exchange)
                    .filter(matchResult -> !matchResult.isMatch())
                    .map(matchResult -> parseToken(authorization))
                    .switchIfEmpty(Mono.empty())
                    .onErrorResume(ex -> fallback(request, authorization, ex));
        }
        //无白名单处理
        return Mono.just(parseToken(authorization))
                .onErrorResume(ex -> fallback(request, authorization, ex));
    }

    private Mono<? extends Authentication> fallback(@Nonnull final ServerHttpRequest request,
                                                    @Nonnull final String authorization,
                                                    @Nonnull final Throwable ex) {
        log.warn("fallback(request-path: {},authorization: " + authorization + ")-exp: {}", request.getPath(), ex.getMessage());
        return Mono.error(new AuthenticationException(ex.getMessage(), ex) {
        });
    }

    @Nonnull
    protected Authentication parseToken(@Nonnull final String authorization) {
        log.debug("parseToken(authorization: {})...", authorization);
        //解析令牌
        final Ticket ticket = authenticationManager.getTokenGenerator().parseToken(authorization);
        final TokenUserDetails userDetails = new TokenUserDetails(ticket);
        //转换用户数据
        return new TokenAuthentication(userDetails, null, userDetails.getAuthorities());
    }
}