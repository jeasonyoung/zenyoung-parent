package top.zenyoung.web.controller.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import top.zenyoung.web.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * 响应工具类
 *
 * @author young
 */
public class RespJsonUtils {

    /**
     * 构建响应
     *
     * @param objectMapper Json工具对象
     * @param response     响应对象
     * @param respResult   响应数据
     */
    @SneakyThrows({IOException.class})
    public static void buildResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nonnull final ResultVO<?> respResult) {
        final Integer code = respResult.getCode();
        response.setStatus(code == null || code != 0 ? HttpStatus.OK.value() : code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), respResult);
        response.flushBuffer();
    }

    /**
     * 构建成功响应
     *
     * @param objectMapper Json工具对象
     * @param response     响应对象
     * @param data         响应数据
     * @param <T>          响应数据类型
     */
    public static <T extends Serializable> void buildSuccessResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final T data) {
        buildResp(objectMapper, response, ResultVO.ofSuccess(data));
    }

    /**
     * 构建失败响应
     *
     * @param objectMapper Json工具对象
     * @param response     响应对象
     * @param status       响应状态
     * @param err          错误消息
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final Integer status, @Nullable final String err) {
        final ResultVO<?> respResult = ResultVO.ofFail(err);
        if (status != null) {
            respResult.setCode(status);
        }
        buildResp(objectMapper, response, respResult);
    }

    /**
     * 构建失败响应
     *
     * @param objectMapper Json工具对象
     * @param response     响应对象
     * @param err          错误消息
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final String err) {
        buildFailResp(objectMapper, response, null, err);
    }

    /**
     * 构建失败响应
     *
     * @param objectMapper Json工具对象
     * @param response     响应对象
     * @param httpStatus   响应状态码
     * @param e            异常数据
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final HttpStatus httpStatus, @Nullable final Throwable e) {
        final String err = e == null ? "未知错误" : e.getMessage();
        buildFailResp(objectMapper, response, httpStatus == null ? null : httpStatus.value(), err);
    }

    /**
     * 构建失败响应
     *
     * @param objectMapper Json工具对象
     * @param response     响应对象
     * @param e            异常数据
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objectMapper, @Nonnull final HttpServletResponse response, @Nullable final Throwable e) {
        buildFailResp(objectMapper, response, null, e);
    }
}
