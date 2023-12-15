package top.zenyoung.boot.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.util.HttpUtils;

import javax.annotation.Nonnull;

/**
 * Http 过滤器
 *
 * @author young
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AppHttpFilter implements WebFilter {

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(context -> context.put(HttpUtils.Info.CONTEXT_KEY, exchange));
    }
}
