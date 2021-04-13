package top.zenyoung.web.controller.filter;

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
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import top.zenyoung.web.controller.util.HttpUtils;
import top.zenyoung.web.util.LogWriter;
import top.zenyoung.web.util.LogWriterDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

/**
 * 请求日志-过滤器-基类
 *
 * @author yangyong
 * @version 1.0
 * 2020/4/1 7:52 上午
 **/
@Slf4j
public abstract class BaseLogFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return -10;
    }

    protected boolean checkLogContentType(@Nullable final MediaType contentType) {
        return true;
    }

    @Nonnull
    protected LogWriter getLogWriter() {
        return new LogWriterDefault();
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        log.debug("filter(exchange: {},chain: {})...", exchange, chain);
        //日志记录对象
        final LogWriter logWriter = getLogWriter();
        //链式传递
        return chain.filter(exchange.mutate()
                //请求处理
                .request(new RecorderServerHttpRequestDecorator(this, logWriter, exchange.getRequest()))
                //响应处理
                .response(new RecorderServerHttpResponseDecorator(this, logWriter, exchange.getResponse()))
                .build()
        );
    }

    /**
     * 构建请求头日志
     *
     * @param logWriter 日志记录器
     * @param method    请求方法
     * @param uri       请求URL
     * @param address   请求地址
     * @param headers   请求头消息
     * @param reqParams 请求参数集合
     */
    protected void buildRequestHeaderLog(@Nonnull final LogWriter logWriter, @Nullable final HttpMethod method, @Nonnull final URI uri,
                                         @Nullable final InetSocketAddress address, @Nonnull final HttpHeaders headers,
                                         @Nullable final MultiValueMap<String, String> reqParams) {
        logWriter.writer("request", new LinkedHashMap<>(5) {
            {
                if (method != null) {
                    put("method", method.name());
                }
                put("path", uri.getPath());
                if (address != null) {
                    put("client", HttpUtils.getClientIpAddr(address));
                }
                if (!CollectionUtils.isEmpty(headers)) {
                    put("headers", buildMultiValue(headers));
                }
                if (!CollectionUtils.isEmpty(reqParams)) {
                    put("query", buildMultiValue(reqParams));
                }
            }
        });
    }

    protected Serializable buildMultiValue(@Nullable final MultiValueMap<String, String> data) {
        if (!CollectionUtils.isEmpty(data)) {
            return new LinkedHashMap<String, Serializable>() {
                {
                    data.forEach((k, v) -> {
                        if (!CollectionUtils.isEmpty(v)) {
                            put(k, Joiner.on(",").skipNulls().join(v));
                        }
                    });
                }
            };
        }
        return null;
    }

    /**
     * 构建请求报文体
     *
     * @param logWriter 日志记录器
     * @param content   报文体内容
     */
    protected void buildRequestBodyLog(@Nonnull final LogWriter logWriter, @Nullable final byte[] content) {
        if (content != null && content.length > 0) {
            logWriter.writer("body", new String(content, StandardCharsets.UTF_8));
        }
    }

    /**
     * 构建响应日志
     *
     * @param logWriter 日志记录器
     * @param status    HTTP状态
     * @param headers   响应报文头
     * @param content   响应报文体
     */
    protected void buildResponseLog(@Nonnull final LogWriter logWriter, @Nullable final HttpStatus status, @Nullable final HttpHeaders headers, @Nullable final byte[] content) {
        logWriter.writer("response", new LinkedHashMap<>(3) {
            {
                if (status != null) {
                    put("status", status.value());
                }
                if (!CollectionUtils.isEmpty(headers)) {
                    put("headers", buildMultiValue(headers));
                }
                if (content != null && content.length > 0) {
                    put("body", new String(content, StandardCharsets.UTF_8));
                }
            }
        });
        log.info("报文日志:[\n{}\n]", logWriter.outputLogs());
    }

    private static class RecorderServerHttpRequestDecorator extends ServerHttpRequestDecorator {

        private final Flux<DataBuffer> body;

        public RecorderServerHttpRequestDecorator(@Nonnull final BaseLogFilter filter, @Nonnull final LogWriter logWriter, @Nonnull final ServerHttpRequest request) {
            super(request);
            //请求报文处理
            final Flux<DataBuffer> flux = super.getBody();
            if (filter.checkLogContentType(getHeaders().getContentType())) {
                filter.buildRequestHeaderLog(logWriter, getMethod(), getURI(), getRemoteAddress(), getHeaders(), request.getQueryParams());
                body = flux.publishOn(Schedulers.single())
                        .map(buffer -> {
                            try {
                                final byte[] bytes = IOUtils.toByteArray(buffer.asInputStream());
                                filter.buildRequestBodyLog(logWriter, bytes);
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
        private final LogWriter logWriter;

        public RecorderServerHttpResponseDecorator(@Nonnull final BaseLogFilter logFilter, @Nonnull final LogWriter logWriter, @Nonnull final ServerHttpResponse response) {
            super(response);
            this.bufferFactory = response.bufferFactory();
            this.filter = logFilter;
            this.logWriter = logWriter;
        }

        @Nonnull
        @Override
        @SuppressWarnings({"unchecked"})
        public Mono<Void> writeWith(@Nonnull final Publisher<? extends DataBuffer> body) {
            if (filter.checkLogContentType(getHeaders().getContentType())) {
                if (body instanceof Flux) {
                    final Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuf -> {
                        final byte[] content = new byte[dataBuf.readableByteCount()];
                        dataBuf.read(content);
                        filter.buildResponseLog(logWriter, getStatusCode(), getHeaders(), content);
                        return bufferFactory.wrap(content);
                    }));
                }
                if (body instanceof Mono) {
                    final Mono<? extends DataBuffer> monoBody = (Mono<? extends DataBuffer>) body;
                    return super.writeWith(monoBody.map(dataBuf -> {
                        final byte[] content = new byte[dataBuf.readableByteCount()];
                        dataBuf.read(content);
                        filter.buildResponseLog(logWriter, getStatusCode(), getHeaders(), content);
                        return bufferFactory.wrap(content);
                    }));
                }
                log.info(logWriter.toString());
            }
            return super.writeWith(body);
        }
    }
}
