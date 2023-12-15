package top.zenyoung.boot.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * 参数分解器接口
 *
 * @author young
 */
public interface ArgumentResolver extends HandlerMethodArgumentResolver {
    /**
     * 分解参数处理
     *
     * @param parameter      the method parameter
     * @param bindingContext the binding context to use
     * @param exchange       the current exchange
     * @return 参数数据
     */
    @Nonnull
    @Override
    default Mono<Object> resolveArgument(@Nonnull final MethodParameter parameter,
                                         @Nonnull final BindingContext bindingContext,
                                         @Nonnull final ServerWebExchange exchange) {
        return resolveArgument(parameter, exchange.getRequest());
    }

    /**
     * 分解参数处理
     *
     * @param parameter MethodParameter
     * @param req       HttpServletRequest
     * @return 参数数据
     */
    Mono<Object> resolveArgument(@Nonnull final MethodParameter parameter, @Nonnull final ServerHttpRequest req);
}
