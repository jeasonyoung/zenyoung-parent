package top.zenyoung.boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zenyoung.boot.util.RespJsonUtils;
import top.zenyoung.common.vo.ResultVO;

import java.util.Map;
import java.util.Objects;

/**
 * 错误异常处理-控制器
 *
 * @author young
 */
@RestController
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrorController extends AbstractErrorController {
    private final ObjectMapper objMapper;

    public ErrorController(final ErrorAttributes attributes, final ObjectMapper objMapper) {
        super(attributes);
        this.objMapper = objMapper;
    }

    private ResultVO<Map<String, Object>> buildResult(final HttpServletRequest req) {
        final HttpStatus httpStatus = super.getStatus(req);
        final Map<String, Object> body = super.getErrorAttributes(req, ErrorAttributeOptions.defaults());
        final ResultVO<Map<String, Object>> vo = ResultVO.ofSuccess(body);
        if (Objects.nonNull(httpStatus)) {
            vo.setCode(httpStatus.value());
            vo.setMessage(httpStatus.getReasonPhrase());
        }
        return vo;
    }

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public void errorHtml(final HttpServletRequest req, final HttpServletResponse res) {
        final HttpStatus httpStatus = super.getStatus(req);
        final ResultVO<?> vo = buildResult(req);
        RespJsonUtils.buildResp(objMapper, res, httpStatus.value(), vo);
    }

    @RequestMapping
    public ResultVO<Map<String, Object>> error(final HttpServletRequest req) {
        return buildResult(req);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResultVO<Map<String, Object>> mediaTypeNotAcceptable(final HttpServletRequest req) {
        return buildResult(req);
    }
}
