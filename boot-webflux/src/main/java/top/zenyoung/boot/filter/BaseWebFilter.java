package top.zenyoung.boot.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.util.HttpUtils;

import javax.annotation.Nonnull;

/**
 * 过滤器基类
 *
 * @author young
 */
@RequiredArgsConstructor
public abstract class BaseWebFilter implements WebFilter {
    private final RequestMappingHandlerMapping handlerMapping;

    /**
     * 是否支持忽略过滤器
     *
     * @param request 请求处理
     * @return 是否忽略
     */
    protected boolean ignoreFilter(@Nonnull final ServerHttpRequest request) {
        return false;
    }

    protected Mono<Void> chainHandler(@Nonnull final Mono<Void> mono, @Nonnull final ServerWebExchange exchange) {
        return HttpUtils.setWebExchange(mono, exchange);
    }

    @Nonnull
    @Override
    public final Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        //是否忽略过滤器
        if (ignoreFilter(request)) {
            final Mono<Void> mono = chain.filter(exchange);
            return chainHandler(mono, exchange);
        }
        //获取处理器
        return handlerMapping.getHandler(exchange)
                .flatMap(method -> {
                    Mono<Void> mono;
                    if (method instanceof HandlerMethod m) {
                        mono = handler(exchange, chain, m);
                    } else {
                        mono = chain.filter(exchange);
                    }
                    return chainHandler(mono, exchange);
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
