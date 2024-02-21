package top.zenyoung.boot.filter;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.annotation.authority.HasAnonymous;
import top.zenyoung.boot.constant.HeaderConstants;
import top.zenyoung.boot.enums.ExceptionEnums;
import top.zenyoung.boot.util.SecurityUtils;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Optional;

/**
 * 令牌处理-过滤器基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseTokenFilter extends BaseWebFilter implements Ordered {
    private static final String TOKEN_NAME = HttpHeaders.AUTHORIZATION;

    protected BaseTokenFilter(@Nonnull final RequestMappingHandlerMapping handlerMapping) {
        super(handlerMapping);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Nonnull
    @Override
    protected final Mono<Void> handler(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain,
                                       @Nonnull final HandlerMethod method) {
        if (method.hasMethodAnnotation(HasAnonymous.class)) {
            return chain.filter(exchange);
        }
        final ServerHttpRequest req = exchange.getRequest();
        //获取令牌
        final String token = Optional.ofNullable(req.getHeaders().getFirst(TOKEN_NAME))
                .filter(val -> !Strings.isNullOrEmpty(val))
                .orElseGet(() -> req.getQueryParams().getFirst(TOKEN_NAME));
        if (Strings.isNullOrEmpty(token)) {
            log.error("未获取用户令牌数据");
            return Mono.error(new ServiceException(ExceptionEnums.UNAUTHORIZED));
        }
        final String bearPrefix = HeaderConstants.BEARER_PREFIX;
        final String accessToken = (token.startsWith(bearPrefix) ? token.replace(bearPrefix, "") : token).trim();
        return parseAccessToken(accessToken)
                .switchIfEmpty(Mono.error(new ServiceException(ExceptionEnums.UNAUTHORIZED)))
                .flatMap(principal -> {
                    exchange.getRequest().getHeaders()
                            .put(HeaderConstants.ACCOUNT_ID, Collections.singletonList(principal.getId() + ""));
                    return chain.filter(exchange)
                            .contextWrite(context -> SecurityUtils.withPrincipal(context, principal));
                });
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
