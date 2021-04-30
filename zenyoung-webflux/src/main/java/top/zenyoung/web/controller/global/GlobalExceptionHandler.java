package top.zenyoung.web.controller.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import top.zenyoung.web.BaseExceptionController;
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
public class GlobalExceptionHandler extends BaseExceptionController {

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
