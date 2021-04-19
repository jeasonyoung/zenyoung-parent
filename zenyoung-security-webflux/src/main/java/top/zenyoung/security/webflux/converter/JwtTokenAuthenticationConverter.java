package top.zenyoung.security.webflux.converter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.model.TokenAuthentication;
import top.zenyoung.security.model.TokenUserDetails;
import top.zenyoung.security.webflux.ZyAuthenticationManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jwt令牌认证转换器类
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/19 4:02 下午
 **/
@Slf4j
public class JwtTokenAuthenticationConverter implements ServerAuthenticationConverter {
    private static final String TOKEN_HEADER_NAME = HttpHeaders.AUTHORIZATION;

    private final ZyAuthenticationManager authenticationManager;
    private final ServerWebExchangeMatcher exchangeMatcher;

    public JwtTokenAuthenticationConverter(@Nonnull final ZyAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        final String[] whiteUrls = buildWhiteUrls(authenticationManager);
        exchangeMatcher = whiteUrls == null || whiteUrls.length == 0 ? null : ServerWebExchangeMatchers.pathMatchers(whiteUrls);
    }

    private String[] buildWhiteUrls(@Nonnull final ZyAuthenticationManager authenticationManager) {
        final List<String> whiteUrls = Lists.newLinkedList();
        //用户登录
        final String[] loginUrls = authenticationManager.getLoginUrls();
        if (loginUrls.length > 0) {
            whiteUrls.addAll(Arrays.stream(loginUrls)
                    .filter(val -> !Strings.isNullOrEmpty(val))
                    .collect(Collectors.toList())
            );
        }
        //白名单
        final String[] urls = authenticationManager.getWhiteUrls();
        if (urls != null && urls.length > 0) {
            whiteUrls.addAll(Arrays.stream(urls)
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .collect(Collectors.toList())
            );
        }
        return whiteUrls.size() > 0 ? whiteUrls.toArray(new String[0]) : null;
    }

    @Override
    public Mono<Authentication> convert(@Nonnull final ServerWebExchange exchange) {
        //获取令牌
        final ServerHttpRequest request = exchange.getRequest();
        final String authorization = request.getHeaders().getFirst(TOKEN_HEADER_NAME);
        //检查是否有白名单
        if (exchangeMatcher != null) {
            //白名单处理
            return exchangeMatcher.matches(exchange)
                    .filter(matchResult -> !matchResult.isMatch())
                    .map(matchResult -> parseToken(request.getPath(), authorization))
                    .switchIfEmpty(Mono.empty())
                    .onErrorResume(ex -> fallback(request, authorization, ex));
        }
        //无白名单处理
        return Mono.just(parseToken(request.getPath(), authorization))
                .onErrorResume(ex -> fallback(request, authorization, ex));
    }

    private Mono<? extends Authentication> fallback(@Nonnull final ServerHttpRequest request,
                                                    @Nullable final String authorization,
                                                    @Nonnull final Throwable ex) {
        log.debug("fallback(request-path: {},authorization: " + authorization + ")-exp: {}", request.getPath(), ex.getMessage());
        if (ex instanceof AuthenticationException) {
            return Mono.error(ex);
        }
        return Mono.error(new AuthenticationException(ex.getMessage(), ex) {
        });
    }

    @Nonnull
    protected Authentication parseToken(@Nonnull final RequestPath path, @Nullable final String authorization) {
        log.debug("parseToken(authorization: {})...", authorization);
        if (Strings.isNullOrEmpty(authorization)) {
            throw new TokenException("令牌为空");
        }
        //解析令牌
        final TokenUserDetails userDetails = new TokenUserDetails(authenticationManager.parseToken(authorization));
        log.info("parseToken(authorization: {})=> {}", authorization, userDetails);
        //转换用户数据
        return new TokenAuthentication(path, userDetails, null, userDetails.getAuthorities());
    }
}