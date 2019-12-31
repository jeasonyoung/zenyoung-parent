package top.zenyoung.webflux.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import top.zenyoung.common.response.RespResult;
import top.zenyoung.common.response.ResultCode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * 响应工具类
 *
 * @author yangyong
 * @version 1.0
 * @date 2019/10/31 4:36 下午
 **/
@Slf4j
public class RespUtils {

    public static Mono<Void> buildResponse(@Nonnull final ServerHttpResponse response, @Nonnull final ObjectMapper objectMapper, @Nonnull final HttpStatus status, @Nullable final Throwable e) {
        return buildResponse(response, objectMapper, status.value(), e);
    }

    public static Mono<Void> buildResponse(@Nonnull final ServerHttpResponse response, @Nonnull final ObjectMapper objectMapper, @Nonnull final ResultCode status, @Nullable final Throwable e) {
        return buildResponse(response, objectMapper, status.getVal(), e);
    }

    public static Mono<Void> buildResponse(@Nonnull final ServerHttpResponse response, @Nonnull final ObjectMapper objectMapper, @Nonnull final Integer statusCode, @Nullable final Throwable e) {
        final String msg = e == null ? "未知错误" : (e.getCause() == null ? e.getMessage() : e.getMessage() + ":" + e.getCause().getClass());
        return buildResponse(response, objectMapper, statusCode, msg);
    }

    public static Mono<Void> buildResponse(@Nonnull final ServerHttpResponse response, @Nonnull final ObjectMapper objectMapper, @Nonnull final Integer statusCode, @Nullable final String err) {
        return buildResponse(response, objectMapper, RespResult.builder().build().buildResult(statusCode, err));
    }

    public static Mono<Void> buildResponse(@Nonnull final ServerHttpResponse response, @Nonnull final ObjectMapper objectMapper, @Nonnull final RespResult<? extends Serializable> respResult) {
        byte[] respData = null;
        try {
            final String json = objectMapper.writeValueAsString(respResult);
            respData = json.getBytes();
        } catch (Throwable ex) {
            log.warn("buildResponse(respResult: {})-exp: {}", respResult, ex.getMessage());
        }
        response.setStatusCode(HttpStatus.valueOf(respResult.getCode()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.justOrEmpty(respData == null ? null : response.bufferFactory().wrap(respData)));
    }
}