package top.zenyoung.boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.common.paging.PageList;
import top.zenyoung.common.util.JsonUtils;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 控制器-基类
 *
 * @author yangyong
 **/
public class BaseController {
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * 成功响应
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> success(@Nullable final T data) {
        return ResultVO.ofSuccess(data);
    }

    /**
     * 成功响应
     *
     * @param dataResult 业务数据
     * @param <T>        数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<PageList<T>> success(@Nullable final PageList<T> dataResult) {
        return ResultVO.ofSuccess(dataResult);
    }

    /**
     * 成功响应
     *
     * @param <T> 响应结果类型
     * @return 响应结果
     */
    protected <T> ResultVO<T> success() {
        return ResultVO.ofSuccess();
    }

    /**
     * 失败响应
     *
     * @param data    失败数据
     * @param code    失败代码
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final T data, @Nullable final Integer code, @Nullable final String message) {
        final ResultVO<T> ret = ResultVO.ofFail();
        if (Objects.nonNull(data)) {
            ret.setData(data);
        }
        if (Objects.nonNull(code)) {
            ret.setCode(code);
        }
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }
        return ret;
    }

    /**
     * 失败响应
     *
     * @param code    失败代码
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final Integer code, @Nullable final String message) {
        return failed(null, code, message);
    }

    /**
     * 失败响应
     *
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final String message) {
        return failed(null, null, message);
    }

    /**
     * 失败响应
     *
     * @param data 失败数据
     * @param <T>  数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final T data) {
        return failed(data, null, null);
    }

    /**
     * 失败响应
     *
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed() {
        return failed((T) null);
    }

    /**
     * 响应失败
     *
     * @param e   失败异常
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final EnumValue e) {
        if (Objects.isNull(e)) {
            return ResultVO.ofFail();
        }
        return ResultVO.of(e);
    }

    /**
     * 响应失败
     *
     * @param e   失败异常
     * @param <T> 数据类型
     * @return 响应数据
     */
    protected <T> ResultVO<T> failed(@Nullable final Throwable e) {
        if (Objects.isNull(e)) {
            return failed();
        }
        return ResultVO.ofFail(e);
    }

    /**
     * 导出Excel处理
     *
     * @param res           响应对象
     * @param fileName      文件名
     * @param exportHandler 导出业务处理
     * @throws IOException IO异常处理
     */
    protected void exportExcel(@Nonnull final HttpServletResponse res, @Nonnull final String fileName,
                               @Nonnull final Consumer<OutputStream> exportHandler) throws IOException {
        final String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        this.export(res, fileName, contentType, "xlsx", exportHandler);
    }

    /**
     * 导出Zip处理
     *
     * @param res           响应对象
     * @param fileName      文件名
     * @param exportHandler 导出业务处理
     * @throws IOException IO异常处理
     */
    protected void exportZip(@Nonnull final HttpServletResponse res, @Nonnull final String fileName,
                             @Nonnull final Consumer<OutputStream> exportHandler) throws IOException {
        final String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        this.export(res, fileName, "zip", contentType, exportHandler);
    }

    /**
     * 导出处理
     *
     * @param res           响应对象
     * @param fileName      文件名
     * @param defExt        默认后缀
     * @param contentType   ContentType
     * @param exportHandler 导出流处理
     * @throws IOException 异常处理
     */
    protected void export(@Nonnull final HttpServletResponse res, @Nonnull final String fileName, @Nonnull final String defExt,
                          @Nonnull final String contentType, @Nonnull final Consumer<OutputStream> exportHandler) throws IOException {
        final String enc = StandardCharsets.UTF_8.name();
        try (final OutputStream outputStream = res.getOutputStream()) {
            res.setContentType(contentType);
            res.setCharacterEncoding(enc);
            String ext = FilenameUtils.getExtension(fileName);
            final String sep = ".";
            if (!Strings.isNullOrEmpty(ext)) {
                ext = sep + ext;
            } else if (!Strings.isNullOrEmpty(defExt)) {
                ext = defExt.startsWith(sep) ? defExt : "." + defExt;
            }
            final String exportFileName = URLEncoder.encode(FilenameUtils.getBaseName(fileName), enc).replace("\\+", "%20");
            res.setHeader("Content-disposition", "attachment;filename*=utf-8''" + exportFileName + ext);
            //业务处理
            exportHandler.accept(outputStream);
            res.flushBuffer();
        } catch (IOException e) {
            res.reset();
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding(enc);
            res.getWriter().write(JsonUtils.toJson(objectMapper, failed(null, buildExpError(e))));
        }
    }

    private static String buildExpError(@Nonnull final Throwable e) {
        String msg = e.getMessage();
        Throwable cause = e;
        while (Strings.isNullOrEmpty(msg) && Objects.nonNull(cause)) {
            cause = cause.getCause();
        }
        return msg;
    }
}
