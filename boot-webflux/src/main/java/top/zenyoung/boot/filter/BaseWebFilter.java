package top.zenyoung.boot.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 过滤器基类
 *
 * @author young
 */
@RequiredArgsConstructor
public abstract class BaseWebFilter implements WebFilter {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 是否支持忽略过滤器
     *
     * @param request 请求处理
     * @return 是否忽略
     */
    protected boolean ignoreFilter(@Nonnull final ServerHttpRequest request) {
        return false;
    }

    @Nonnull
    @Override
    public final Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        //是否忽略过滤器
        if (ignoreFilter(request)) {
            return chain.filter(exchange);
        }
        //获取处理器
        return requestMappingHandlerMapping.getHandler(exchange)
                .flatMap(method -> {
                    if (method instanceof HandlerMethod m) {
                        return handler(exchange, chain, m);
                    }
                    return chain.filter(exchange);
                });
    }

    /**
     * 过滤处理器
     *
     * @param exchange ServerWebExchange
     * @param chain    WebFilterChain
     * @param method   HandlerMethod
     * @return 处理结果
     */
    @Nonnull
    protected abstract Mono<Void> handler(@Nonnull final ServerWebExchange exchange,
                                          @Nonnull final WebFilterChain chain,
                                          @Nonnull final HandlerMethod method);
}
