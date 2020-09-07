package top.zenyoung.controller.utl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;

/**
 * 响应工具类
 *
 * @author yangyong
 * @version 1.0
 * 2020/3/20 6:23 下午
 **/
@Slf4j
public class RespJsonUtils {
    private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();

    public static Mono<Void> buildFailResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final ServerHttpResponse response, @Nonnull final HttpStatus httpStatus, @Nonnull final Throwable e) {
        return buildResp(objectMapper, response, RespResult.of(httpStatus.value(), e.getMessage(), null));
    }

    public static Mono<Void> buildFailResp(@Nonnull final ServerHttpResponse response, @Nonnull final HttpStatus httpStatus, @Nonnull final Throwable e) {
        return buildFailResp(OBJ_MAPPER, response, httpStatus, e);
    }

    public static Mono<Void> buildSuccessResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final ServerHttpResponse response, @Nonnull final RespResult<?> respResult) {
        return buildResp(objectMapper, response, respResult);
    }

    public static Mono<Void> buildSuccessResp(@Nonnull final ServerHttpResponse response, @Nonnull final RespResult<?> respResult) {
        return buildResp(OBJ_MAPPER, response, respResult);
    }

    public static Mono<Void> buildResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final ServerHttpResponse response, @Nonnull final RespResult<?> respResult) {
        //输出数据处理
        final DataBuffer dataBuffer = response.bufferFactory().wrap(JsonUtils.toJsonBytes(objectMapper, respResult));
        //输出JSON处理
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeAndFlushWith(Mono.just(ByteBufMono.just(dataBuffer)));
    }
}
