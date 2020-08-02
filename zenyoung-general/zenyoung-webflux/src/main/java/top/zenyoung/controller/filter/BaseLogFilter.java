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
import org.springframework.util.MultiValueMap;
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
 * 2020/4/1 7:52 上午
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
    protected LogFilterWriter getLogWriter() {
        return new LogFilterWriterDefault();
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull final ServerWebExchange exchange, @Nonnull final WebFilterChain chain) {
        log.debug("filter(exchange: {},chain: {})...", exchange, chain);
        //日志记录对象
        final LogFilterWriter logWriter = getLogWriter();
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
    protected void buildRequestHeaderLog(@Nonnull final LogFilterWriter logWriter, @Nullable final HttpMethod method, @Nonnull final URI uri,
                                         @Nullable final InetSocketAddress address, @Nonnull final HttpHeaders headers,
                                         @Nullable final MultiValueMap<String, String> reqParams) {
        logWriter.writer(REQUEST_START)
                .writer(method == null ? "" : method.name()).writer(": ").writer(uri.getPath())
                .writer("\nclient: ").writer(address == null ? "" : address.getHostName() + "," + address.getPort())
                .writer("\nheaders: ").writer(buildMultiValue(headers));
        if (!CollectionUtils.isEmpty(reqParams)) {
            logWriter.writer("\nquery: ").writer(buildMultiValue(reqParams));
        }
    }

    /**
     * 构建请求报文体
     *
     * @param logWriter 日志记录器
     * @param content   报文体内容
     */
    protected void buildRequestBodyLog(@Nonnull final LogFilterWriter logWriter, @Nullable final byte[] content) {
        logWriter.writer("\nbody: ")
                .writer((content != null && content.length > 0) ? new String(content, StandardCharsets.UTF_8) : null)
                .writer(REQUEST_END);
    }

    /**
     * 构建响应日志
     *
     * @param logWriter 日志记录器
     * @param status    HTTP状态
     * @param headers   响应报文头
     * @param content   响应报文体
     */
    protected void buildResponseLog(@Nonnull final LogFilterWriter logWriter, @Nullable final HttpStatus status, @Nullable final HttpHeaders headers, @Nullable final byte[] content) {
        logWriter.writer(RESPONSE_START)
                .writer(status == null ? null : status.toString())
                .writer("\nheaders: ")
                .writer(buildMultiValue(headers))
                .writer("\nbody: ")
                .writer((content != null && content.length > 0) ? new String(content, StandardCharsets.UTF_8) : null)
                .writer(RESPONSE_END);
        log.info("报文日志:[\n{}\n]", logWriter.outputLogs());
    }

    /**
     * 构建数据整合处理
     *
     * @param data 数据集合
     * @return 整合结果
     */
    protected String buildMultiValue(@Nullable final MultiValueMap<String, String> data) {
        if (!CollectionUtils.isEmpty(data)) {
            return data.entrySet().stream()
                    .map(entry -> {
                        final List<String> vals = entry.getValue();
                        return entry.getKey() + "=" + (CollectionUtils.isEmpty(vals) ? "" : Joiner.on(",").skipNulls().join(vals));
                    })
                    .collect(Collectors.joining("\n\t"));
        }
        return null;
    }


    private static class RecorderServerHttpRequestDecorator extends ServerHttpRequestDecorator {

        private final Flux<DataBuffer> body;

        public RecorderServerHttpRequestDecorator(@Nonnull final BaseLogFilter filter, @Nonnull final LogFilterWriter logWriter, @Nonnull final ServerHttpRequest request) {
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
        private final LogFilterWriter logWriter;


        public RecorderServerHttpResponseDecorator(@Nonnull final BaseLogFilter logFilter, @Nonnull final LogFilterWriter logWriter, @Nonnull final ServerHttpResponse response) {
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
