package top.zenyoung.boot.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import top.zenyoung.boot.controller.BaseController;
import top.zenyoung.common.exception.BaseException;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * 统一异常处理控制器
 *
 * @author young
 */
@Slf4j
@RestControllerAdvice
public class ResponseAdviceController extends BaseController implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@Nonnull final MethodParameter returnType, @Nonnull final Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType() != Void.class;
    }

    @Override
    public Object beforeBodyWrite(@Nullable final Object body, @Nonnull final MethodParameter returnType, @Nonnull final MediaType selectedContentType,
                                  @Nonnull final Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @Nonnull final ServerHttpRequest req, @Nonnull final ServerHttpResponse res) {
        if (body instanceof ResultVO<?>) {
            return body;
        }
        return success(body);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResultVO<String> handleMethodArgumentNotValidException(@Nonnull final MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({BindException.class})
    public ResultVO<String> handlerBindException(@Nonnull final BindException e) {
        log.warn("handlerBindException(e: {})...", e.getMessage());
        final StringBuilder builder = new StringBuilder("Validation failed for ");
        BindingResult bindingResult = e.getBindingResult();
        if (bindingResult.getErrorCount() > 1) {
            builder.append(" with ").append(bindingResult.getErrorCount()).append(" errors");
        }
        builder.append(": ");
        for (ObjectError error : bindingResult.getAllErrors()) {
            builder.append("[").append(error).append("] ");
        }
        return failed(builder.toString());
    }

    @ExceptionHandler({ValidationException.class})
    public ResultVO<String> handleValidationException(@Nonnull final ValidationException e) {
        log.warn("handleValidationException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResultVO<String> handleConstraintViolationException(@Nonnull final ConstraintViolationException e) {
        log.warn("handleConstraintViolationException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({ServiceException.class})
    public ResultVO<String> handleException(@Nonnull final ServiceException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e.getVal(), e.getMessage());
    }

    @ExceptionHandler({BaseException.class})
    public ResultVO<String> handleException(@Nonnull final BaseException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e.getVal(), e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public ResultVO<String> handleException(@Nonnull final Exception e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResultVO<String> handleException(@Nonnull final RuntimeException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e);
    }
}
