package top.zenyoung.web.controller.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * 全局异常处理
 *
 * @author young
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public RespResult<?> handleMethodArgumentNotValidException(@Nonnull final MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException(e: {})...", e.getMessage());
        return RespResult.ofFail(e.getMessage());
    }

    @ExceptionHandler({ValidationException.class})
    public RespResult<?> handleValidationException(@Nonnull final ValidationException e) {
        log.warn("handleValidationException(e: {})...", e.getMessage());
        return RespResult.ofFail(e.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public RespResult<?> handleConstraintViolationException(@Nonnull final ConstraintViolationException e) {
        log.warn("handleConstraintViolationException(e: {})...", e.getMessage());
        return RespResult.ofFail(e.getMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    public RespResult<?> handlerNoFoundException(@Nonnull final NoHandlerFoundException e) {
        log.warn("handlerNoFoundException(e: {})...", e.getMessage());
        return RespResult.of(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
    }

    @ExceptionHandler({DuplicateKeyException.class})
    public RespResult<?> handleDuplicateKeyException(@Nonnull final DuplicateKeyException e) {
        log.warn("handleDuplicateKeyException(e: {})...", e.getMessage());
        return RespResult.ofFail(e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public RespResult<?> handleException(@Nonnull final Exception e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return RespResult.ofFail(e.getMessage());
    }
}
