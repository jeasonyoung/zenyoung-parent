package top.zenyoung.boot.filter;

import com.google.common.base.Strings;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * 令牌处理-过滤器基类
 *
 * @author young
 */
public abstract class BaseTokenFilter implements WebFilter, Ordered {
    private static final String TOKEN_NAME = HttpHeaders.AUTHORIZATION;
    private static final String AUTH_BEARER_PREFIX = "Bearer ";

    @Override
    public int getOrder() {
        return -1;
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        final ServerHttpRequest req = exchange.getRequest();
        //获取令牌
        final String token = Optional.ofNullable(req.getHeaders().getFirst(TOKEN_NAME))
                .filter(val -> !Strings.isNullOrEmpty(val))
                .orElseGet(() -> req.getQueryParams().getFirst(TOKEN_NAME));
        if (!Strings.isNullOrEmpty(token)) {
            final String accessToken = (token.startsWith(AUTH_BEARER_PREFIX) ? token.replace(AUTH_BEARER_PREFIX, "") : token).trim();
            if (!Strings.isNullOrEmpty(accessToken)) {
                return parseAccessToken(accessToken)
                        .flatMap(principal -> {
                            if (Objects.isNull(principal)) {
                                return chain.filter(exchange);
                            }
                            return chain.filter(exchange)
                                    .contextWrite(context -> SecurityUtils.withPrincipal(context, principal));
                        });
            }
        }
        return chain.filter(exchange);
    }

    /**
     * 解析令牌
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    @Nonnull
    protected abstract Mono<UserPrincipal> parseAccessToken(@Nonnull final String accessToken);
}
