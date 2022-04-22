package top.zenyoung.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import top.zenyoung.common.exception.BaseException;
import top.zenyoung.web.model.ResultCode;
import top.zenyoung.web.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * 异常控制器基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseGlobalExceptionController {

    protected ResultVO<?> fail(@Nonnull final Throwable e) {
        final ResultVO<?> ret = ResultVO.of(ResultCode.Error);
        if (e instanceof BaseException) {
            ret.setCode(((BaseException) e).getCode());
        }
        return ret.setMessage(e.getMessage());
    }

    protected ResultVO<?> fail(@Nonnull final String err) {
        return ResultVO.of(ResultCode.Error).setMessage(err);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResultVO<?> handleMethodArgumentNotValidException(@Nonnull final MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException(e: {})...", e.getMessage());
        return fail(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({BindException.class})
    public ResultVO<?> handlerBindException(@Nonnull final BindException e) {
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
        return fail(builder.toString());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ValidationException.class})
    public ResultVO<?> handleValidationException(@Nonnull final ValidationException e) {
        log.warn("handleValidationException(e: {})...", e.getMessage());
        return fail(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ConstraintViolationException.class})
    public ResultVO<?> handleConstraintViolationException(@Nonnull final ConstraintViolationException e) {
        log.warn("handleConstraintViolationException(e: {})...", e.getMessage());
        return fail(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({DuplicateKeyException.class})
    public ResultVO<?> handleDuplicateKeyException(@Nonnull final DuplicateKeyException e) {
        log.warn("handleDuplicateKeyException(e: {})...", e.getMessage());
        return fail(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({BaseException.class})
    public ResultVO<?> handleException(@Nonnull final BaseException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return fail(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ResultVO<?> handleException(@Nonnull final Exception e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return fail(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class})
    public ResultVO<?> handleException(@Nonnull final RuntimeException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return fail(e);
    }
}
