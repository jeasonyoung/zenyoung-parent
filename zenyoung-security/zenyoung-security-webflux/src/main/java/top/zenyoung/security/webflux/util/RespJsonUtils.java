package top.zenyoung.security.webflux.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import top.zenyoung.common.model.RespResult;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

/**
 * 响应工具类
 *
 * @author yangyong
 * @version 1.0
 *  2020/3/20 6:23 下午
 **/
@Slf4j
public class RespJsonUtils {
    private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();

    public static Mono<Void> buildFailResp(@Nonnull final ServerHttpResponse response, @Nonnull final HttpStatus httpStatus, @Nonnull final Throwable e) {
        return buildResp(response, new RespResult<>(httpStatus.value(), e.getMessage(), null));
    }

    public static Mono<Void> buildSuccessResp(@Nonnull final ServerHttpResponse response, @Nonnull final RespResult<?> respResult) {
        return buildResp(response, respResult);
    }

    private static Mono<Void> buildResp(@Nonnull final ServerHttpResponse response, @Nonnull final RespResult<?> respResult) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        //错误消息内容
        byte[] resp = null;
        try {
            final String json = OBJ_MAPPER.writeValueAsString(respResult);
            resp = json.getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            log.error("buildResp-exp:" + ex.getMessage(), ex);
        }
        return response.writeWith(Mono.justOrEmpty(resp == null ? null : response.bufferFactory().wrap(resp)));
    }
}
