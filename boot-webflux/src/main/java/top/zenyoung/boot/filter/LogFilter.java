package top.zenyoung.boot.filter;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.zenyoung.boot.constant.HeaderConstants;

import javax.annotation.Nonnull;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 日志过滤器
 *
 * @author young
 */
@Slf4j
public class LogFilter extends BaseWebFilter implements Ordered {
    private final IdGeneratorProvider provider;

    /**
     * 构造函数
     *
     * @param handlerMapping RequestMappingHandlerMapping
     */
    public LogFilter(@Nonnull final RequestMappingHandlerMapping handlerMapping,
                     @Nonnull final IdGeneratorProvider generatorProvider) {
        super(handlerMapping);
        this.provider = generatorProvider;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected boolean ignoreFilter(@Nonnull final ServerHttpRequest request) {
        final String scheme = request.getURI().getScheme();
        final boolean isHttpReq = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        return !isHttpReq;
    }

    @Nonnull
    @Override
    protected Mono<Void> handler(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain,
                                 @Nonnull final HandlerMethod method) {
        final ServerHttpRequest httpRequest = exchange.getRequest();
        final HttpMethod httpMethod = httpRequest.getMethod();
        if (httpMethod == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        final long start = System.currentTimeMillis();
        final String reqId = provider.getShare().generateAsString();
        MDC.put(HeaderConstants.TO_REQ_ID, reqId);
        exchange.getAttributes().put(HeaderConstants.TO_REQ_ID, reqId);
        exchange.getAttributes().put(HeaderConstants.TO_REQ_TIME, start);
        final URI uri = httpRequest.getURI();
        final String path = uri.getPath();
        final String query = uri.getQuery();
        final MultiValueMap<String, String> headers = httpRequest.getHeaders();
        log.info("[request][{}]请求: {} {} {} 请求头: {}", method, httpMethod, path, query, headers);
        final BodyCaptureRequest bodyCaptureRequest = new BodyCaptureRequest(exchange.getRequest()) {
            @Nonnull
            @Override
            public Flux<DataBuffer> getBody() {
                return super.getBody()
                        .doFinally(st -> log.info("[request] 请求体: {}", this.getFullBody()));
            }
        };
        final BodyCaptureResponse bodyCaptureResponse = new BodyCaptureResponse(exchange.getResponse()) {
            @Nonnull
            @Override
            public Mono<Void> writeWith(@Nonnull final Publisher<? extends DataBuffer> body) {
                return super.writeWith(body)
                        .doFinally(st -> log.info("[response]响应体: {}", this.getFullBody()));
            }
        };
        final ServerWebExchange build = exchange.mutate()
                .request(bodyCaptureRequest)
                .response(bodyCaptureResponse)
                .build();
        build.getResponse()
                .getHeaders()
                .put(HeaderConstants.TO_REQ_ID, Collections.singletonList(reqId));
        return chain.filter(build)
                .doFinally(st -> log.info("[response {}] 耗时: {}ms", reqId, System.currentTimeMillis() - start));
    }

    private static class BodyCaptureRequest extends ServerHttpRequestDecorator {
        private final StringBuilder body = new StringBuilder();

        public BodyCaptureRequest(@Nonnull final ServerHttpRequest delegate) {
            super(delegate);
        }

        @Nonnull
        public Flux<DataBuffer> getBody() {
            return super.getBody().doOnNext(this::capture);
        }

        private void capture(@Nonnull final DataBuffer buffer) {
            final Charset charset = StandardCharsets.UTF_8;
            try (final DataBuffer.ByteBufferIterator iterator = buffer.readableByteBuffers()) {
                while (iterator.hasNext()) {
                    final ByteBuffer byteBuffer = iterator.next();
                    this.body.append(charset.decode(byteBuffer));
                }
            }
        }

        public String getFullBody() {
            return this.body.toString();
        }
    }

    private static class BodyCaptureResponse extends ServerHttpResponseDecorator {
        private final StringBuilder body = new StringBuilder();

        public BodyCaptureResponse(@Nonnull final ServerHttpResponse delegate) {
            super(delegate);
        }

        @Nonnull
        @Override
        public Mono<Void> writeWith(@Nonnull final Publisher<? extends DataBuffer> body) {
            final Flux<DataBuffer> buffer = Flux.from(body);
            return super.writeWith(buffer.doOnNext(this::capture)).cache();
        }

        private void capture(@Nonnull final DataBuffer buffer) {
            final Charset charset = StandardCharsets.UTF_8;
            try (final DataBuffer.ByteBufferIterator iterator = buffer.readableByteBuffers()) {
                while (iterator.hasNext()) {
                    final ByteBuffer byteBuffer = iterator.next();
                    this.body.append(charset.decode(byteBuffer));
                }
            }
        }

        public String getFullBody() {
            return this.body.toString();
        }
    }
}
