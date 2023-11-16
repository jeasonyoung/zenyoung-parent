package top.zenyoung.boot.advice;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import top.zenyoung.common.exception.BaseException;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.common.vo.ResultVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Objects;

/**
 * 统一异常处理控制器-基类
 */
@Slf4j
public abstract class BaseResponseAdviceController<R> {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public R handleMethodArgumentNotValidException(@Nonnull final MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({BindException.class})
    public R handlerBindException(@Nonnull final BindException e) {
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
    public R handleValidationException(@Nonnull final ValidationException e) {
        log.warn("handleValidationException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public R handleConstraintViolationException(@Nonnull final ConstraintViolationException e) {
        log.warn("handleConstraintViolationException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({ServiceException.class})
    public R handleException(@Nonnull final ServiceException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e.getVal(), e.getMessage());
    }

    @ExceptionHandler({BaseException.class})
    public R handleException(@Nonnull final BaseException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e.getVal(), e.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public R handleException(@Nonnull final Exception e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e);
    }

    @ExceptionHandler({RuntimeException.class})
    public R handleException(@Nonnull final RuntimeException e) {
        log.warn("handleException(e: {})...", e.getMessage());
        return failed(e);
    }

    /**
     * 失败响应
     *
     * @param e 异常对象
     * @return 响应数据
     */
    protected R failed(@Nullable final Throwable e) {
        if (Objects.isNull(e)) {
            return resultHandler(ResultVO.ofFail());
        }
        return resultHandler(ResultVO.ofFail(e));
    }

    /**
     * 失败响应
     *
     * @param err 异常消息
     * @return 响应数据
     */
    protected R failed(@Nullable final String err) {
        if (Strings.isNullOrEmpty(err)) {
            return resultHandler(ResultVO.ofFail());
        }
        return resultHandler(ResultVO.ofFail(err));
    }

    /**
     * 失败响应
     *
     * @param code    失败代码
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应数据
     */
    protected <T> R failed(@Nullable final Integer code, @Nullable final String message) {
        final ResultVO<T> ret = ResultVO.ofFail();
        if (Objects.nonNull(code)) {
            ret.setCode(code);
        }
        if (!Strings.isNullOrEmpty(message)) {
            ret.setMessage(message);
        }
        return resultHandler(ret);
    }

    /**
     * 响应结果处理
     *
     * @param vo  响应结果对象
     * @param <T> 响应数据类型
     * @return 处理结果
     */
    protected abstract <T> R resultHandler(@Nonnull final ResultVO<T> vo);
}
