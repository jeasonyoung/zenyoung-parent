package top.zenyoung.controller.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 响应工具类
 *
 * @author young
 */
public class RespJsonUtils {

    @SneakyThrows
    public static void buildResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nonnull final RespResult<?> respResult) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), respResult);
        response.flushBuffer();
    }

    public static void buildFailResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final HttpStatus httpStatus, @Nullable final Throwable e) {
        final String err = e == null ? "未知错误" : e.getMessage();
        final RespResult<?> respResult = httpStatus == null ? RespResult.ofFail(err) : RespResult.of(httpStatus.value(), err, null);
        buildResp(objectMapper, response, respResult);
    }

    public static void buildFailResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final Throwable e) {
        buildFailResp(objectMapper, response, null, e);
    }

    public static <T extends Serializable> void buildSuccessResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final T data) {
        buildResp(objectMapper, response, RespResult.ofSuccess(data));
    }
}
