package top.zenyoung.controller.filter;

import com.google.common.base.Joiner;
import io.netty.buffer.UnpooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 请求日志-过滤器-基类
 *
 * @author yangyong
 * @version 1.0
 * @date 2020/4/1 7:52 上午
 **/
@Slf4j
public abstract class BaseLogFilter implements WebFilter, Ordered {
    private static final String REQUEST_START = "\nrequest:\n";
    private static final String REQUEST_END = "\n";
    private static final String RESPONSE_START = "\n\nresponse:\n";
    private static final String RESPONSE_END = REQUEST_END;

    @Override
    public int getOrder() {
        return -10;
    }

    private boolean checkLogContentType(@Nullable final MediaType contentType) {
        return true;
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        log.debug("filter(exchange: {},chain: {})...", exchange, chain);
        //日志记录对象
        final StringBuilder logBudiler = new StringBuilder();
        //请求处理
        final RecorderServerHttpRequestDecorator requestDecorator = new RecorderServerHttpRequestDecorator(this, exchange.getRequest(), logBudiler);
        //响应处理
        final RecorderServerHttpResponseDecorator responseDecorator = new RecorderServerHttpResponseDecorator(this, exchange.getResponse(), logBudiler);
        //链式传递
        return chain.filter(exchange.mutate().request(requestDecorator).response(responseDecorator).build());
    }

    protected void buildRequestHeaderLog(@Nonnull final StringBuilder logBudiler, @Nullable final HttpMethod method, @Nonnull final URI uri,
                                         @Nullable final InetSocketAddress address, @Nonnull final HttpHeaders headers) {
        logBudiler.append(REQUEST_START)
                .append(method == null ? "" : method.name()).append(": ").append(uri.getPath())
                .append("\nclient: ").append(address == null ? "" : address.getHostName() + "," + address.getPort())
                .append("\nheaders: ").append(buildHeaders(headers));
    }

    protected void buildRequestBodyLog(@Nonnull final StringBuilder logBudiler, @Nullable final byte[] content) {
        logBudiler.append("\nbody: ").append((content != null && content.length > 0) ? new String(content, StandardCharsets.UTF_8) : null)
                .append(REQUEST_END);
    }

    protected void buildResponseLog(@Nonnull final StringBuilder logBudiler, @Nullable final HttpStatus status, @Nullable final HttpHeaders headers, @Nullable final byte[] content) {
        logBudiler.append(RESPONSE_START)
                .append(status)
                .append("\nheaders: ").append(buildHeaders(headers))
                .append("\nbody: ").append((content != null && content.length > 0) ? new String(content, StandardCharsets.UTF_8) : null)
                .append(RESPONSE_END);
        log.info("报文日志:[\n{}\n]", logBudiler);
    }

    protected String buildHeaders(@Nullable final HttpHeaders headers) {
        if (CollectionUtils.isEmpty(headers)) {
            return null;
        }
        return headers.entrySet().stream()
                .map(entry -> {
                    final List<String> values = entry.getValue();
                    return entry.getKey() + "=" + (CollectionUtils.isEmpty(values) ? "" : Joiner.on(",").skipNulls().join(values));
                })
                .collect(Collectors.joining("\n\t\t"));
    }

    private static class RecorderServerHttpRequestDecorator extends ServerHttpRequestDecorator {

        private final Flux<DataBuffer> body;

        public RecorderServerHttpRequestDecorator(@Nonnull final BaseLogFilter filter, @Nonnull final ServerHttpRequest request, @Nonnull final StringBuilder logBudiler) {
            super(request);
            //请求报文处理
            final Flux<DataBuffer> flux = super.getBody();
            if (filter.checkLogContentType(getHeaders().getContentType())) {
                filter.buildRequestHeaderLog(logBudiler, getMethod(), getURI(), getRemoteAddress(), getHeaders());
                body = flux.publishOn(Schedulers.single())
                        .map(buffer -> {
                            try {
                                final byte[] bytes = IOUtils.toByteArray(buffer.asInputStream());
                                filter.buildRequestBodyLog(logBudiler, bytes);
                                return new NettyDataBufferFactory(new UnpooledByteBufAllocator(false)).wrap(bytes);
                            } catch (IOException e) {
                                log.error("RecorderServerHttpRequestDecorator-exp: {}", e.getMessage());
                            }
                            return null;
                        });
            } else {
                body = flux;
            }
        }

        @Nonnull
        @Override
        public Flux<DataBuffer> getBody() {
            return body;
        }

    }

    private static class RecorderServerHttpResponseDecorator extends ServerHttpResponseDecorator {
        private final DataBufferFactory bufferFactory;

        private final BaseLogFilter filter;
        private final StringBuilder logBudiler;


        public RecorderServerHttpResponseDecorator(@Nonnull final BaseLogFilter logFilter, @Nonnull final ServerHttpResponse response, @Nonnull final StringBuilder logBudiler) {
            super(response);
            this.bufferFactory = response.bufferFactory();
            this.filter = logFilter;
            this.logBudiler = logBudiler;
        }

        @Nonnull
        @SuppressWarnings({"unchecked"})
        @Override
        public Mono<Void> writeWith(@Nonnull final Publisher<? extends DataBuffer> body) {
            if (filter.checkLogContentType(getHeaders().getContentType())) {
                if (body instanceof Flux) {
                    final Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuf -> {
                        final byte[] content = new byte[dataBuf.readableByteCount()];
                        dataBuf.read(content);
                        filter.buildResponseLog(logBudiler, getStatusCode(), getHeaders(), content);
                        return bufferFactory.wrap(content);
                    }));
                }
                if (body instanceof Mono) {
                    final Mono<? extends DataBuffer> monoBody = (Mono<? extends DataBuffer>) body;
                    return super.writeWith(monoBody.map(dataBuf -> {
                        final byte[] content = new byte[dataBuf.readableByteCount()];
                        dataBuf.read(content);
                        filter.buildResponseLog(logBudiler, getStatusCode(), getHeaders(), content);
                        return bufferFactory.wrap(content);
                    }));
                }
                log.info(logBudiler.toString());
            }
            return super.writeWith(body);
        }
    }
}
