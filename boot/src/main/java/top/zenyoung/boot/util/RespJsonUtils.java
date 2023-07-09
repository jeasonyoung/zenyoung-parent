package top.zenyoung.boot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * 响应工具类
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RespJsonUtils {

    /**
     * 构建响应
     *
     * @param objMapper  Json工具对象
     * @param res        响应对象
     * @param statusCode 响应状态码
     * @param data       响应数据
     * @param <T>        响应数据类型
     */
    public static <T> void buildResp(@Nonnull final ObjectMapper objMapper, @Nonnull final HttpServletResponse res,
                                     @Nullable final Integer statusCode, @Nonnull final T data) {
        try {
            if (Objects.nonNull(statusCode)) {
                res.setStatus(Math.max(HttpStatus.OK.value(), statusCode));
            }
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objMapper.writeValue(res.getOutputStream(), data);
            res.flushBuffer();
        } catch (IOException e) {
            log.error("buildResp(statusCode: {},data: {})-exp: {}", statusCode, data, e.getMessage());
        }
    }

    /**
     * 构建响应
     *
     * @param objMapper Json工具对象
     * @param res       响应对象
     * @param vo        响应数据
     */
    public static void buildResp(@Nonnull final ObjectMapper objMapper, @Nonnull final HttpServletResponse res,
                                 @Nonnull final ResultVO<?> vo) {
        buildResp(objMapper, res, vo.getCode(), vo);
    }

    /**
     * 构建成功响应
     *
     * @param objMapper Json工具对象
     * @param res       响应对象
     * @param data      响应数据
     * @param <T>       响应数据类型
     */
    public static <T extends Serializable> void buildSuccessResp(@Nonnull final ObjectMapper objMapper,
                                                                 @Nonnull final HttpServletResponse res,
                                                                 @Nullable final T data) {
        buildResp(objMapper, res, ResultVO.ofSuccess(data));
    }

    /**
     * 构建失败响应
     *
     * @param objMapper  Json工具对象
     * @param res        响应对象
     * @param statusCode 响应状态
     * @param err        错误消息
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objMapper, @Nonnull final HttpServletResponse res,
                                     @Nullable final Integer statusCode, @Nullable final String err) {
        final ResultVO<?> vo = ResultVO.ofFail(err);
        if (Objects.nonNull(statusCode)) {
            vo.setCode(statusCode);
        }
        buildResp(objMapper, res, vo);
    }

    /**
     * 构建失败响应
     *
     * @param objMapper Json工具对象
     * @param res       响应对象
     * @param err       错误消息
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objMapper, @Nonnull final HttpServletResponse res,
                                     @Nullable final String err) {
        buildFailResp(objMapper, res, (HttpStatus) null, err);
    }

    /**
     * 构建失败响应
     *
     * @param objMapper  Json工具对象
     * @param res        响应对象
     * @param httpStatus 响应状态码
     * @param e          异常数据
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objMapper, @Nonnull final HttpServletResponse res,
                                     @Nullable final HttpStatus httpStatus, @Nullable final Throwable e) {
        final String err = Objects.isNull(e) ? "未知错误" : e.getMessage();
        buildFailResp(objMapper, res, httpStatus == null ? null : httpStatus.value(), err);
    }

    /**
     * 构建失败响应
     *
     * @param objMapper  Json工具对象
     * @param res        响应对象
     * @param httpStatus 响应状态码
     * @param msg        消息数据
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objMapper, @Nonnull final HttpServletResponse res,
                                     @Nullable final HttpStatus httpStatus, @Nullable final String msg) {
        final String err = Strings.isNullOrEmpty(msg) ? "未知错误" : msg;
        buildFailResp(objMapper, res, httpStatus == null ? null : httpStatus.value(), err);
    }

    /**
     * 构建失败响应
     *
     * @param objMapper Json工具对象
     * @param res       响应对象
     * @param e         异常数据
     */
    public static void buildFailResp(@Nonnull final ObjectMapper objMapper, @Nonnull final HttpServletResponse res,
                                     @Nullable final Throwable e) {
        buildFailResp(objMapper, res, null, e);
    }
}
