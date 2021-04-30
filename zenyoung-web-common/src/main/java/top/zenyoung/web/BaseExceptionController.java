package top.zenyoung.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import top.zenyoung.web.vo.RespResult;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * 异常控制器基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseExceptionController {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public RespResult<?> handleMethodArgumentNotValidException(@Nonnull final MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException(e: {})...", e.getMessage());
        return RespResult.ofFail(e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public RespResult<?> handleException(@Nonnull final Exception e) {
        log.warn("handleException(e: {})...", e.getMessage());
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

    @ExceptionHandler({DuplicateKeyException.class})
    public RespResult<?> handleDuplicateKeyException(@Nonnull final DuplicateKeyException e) {
        log.warn("handleDuplicateKeyException(e: {})...", e.getMessage());
        return RespResult.ofFail(e.getMessage());
    }
}
