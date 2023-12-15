package top.zenyoung.boot.filter;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import top.zenyoung.boot.constant.AppConstants;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * 追踪ID-过滤器
 *
 * @author young
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class TraceFilter implements WebFilter, AppConstants {
    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(context -> {
                    final ServerHttpRequest request = exchange.getRequest();
                    //header是否有TraceID
                    final String traceId = Optional.of(request)
                            .map(HttpMessage::getHeaders)
                            .map(headers -> headers.getFirst(TRACE_ID))
                            .orElseGet(request::getId);
                    if (Strings.isNullOrEmpty(traceId)) {
                        log.warn("{} not present in header: {}", TRACE_ID, request.getURI());
                    }
                    final Context ctx = context.put(TRACE_ID, traceId);
                    exchange.getAttributes().put(TRACE_ID, traceId);
                    return ctx;
                });
    }
}
