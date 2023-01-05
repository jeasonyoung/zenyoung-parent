package top.zenyoung.boot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import top.zenyoung.common.util.MapUtils;
import top.zenyoung.common.vo.ResultVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public void errorHtml(final HttpServletRequest req, final HttpServletResponse res) {
        final Map<String, Object> body = super.getErrorAttributes(req, ErrorAttributeOptions.defaults());
        RespJsonUtils.buildResp(objMapper, res, getStatus(req).value(), body);
    }

    @RequestMapping
    public ResultVO<?> error(final HttpServletRequest req) {
        final HttpStatus status = super.getStatus(req);
        if (status == HttpStatus.NO_CONTENT) {
            return ResultVO.ofFail(status.getReasonPhrase());
        }
        final Map<String, Object> body = super.getErrorAttributes(req, ErrorAttributeOptions.defaults());
        return MapUtils.to(body, ResultVO.class);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResultVO<?> mediaTypeNotAcceptable(final HttpServletRequest req) {
        final Map<String, Object> body = super.getErrorAttributes(req, ErrorAttributeOptions.defaults());
        return MapUtils.to(body, ResultVO.class);
    }
}
