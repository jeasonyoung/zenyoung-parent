package top.zenyoung.retrofit.exception;

import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.StringUtils;
import top.zenyoung.retrofit.util.RetrofitUtils;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Retrofit-异常
 *
 * @author young
 */
public class RetrofitException extends RuntimeException {
    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   异常对象
     */
    public RetrofitException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public RetrofitException(final String message) {
        super(message);
    }

    public static RetrofitException errorStatus(@Nonnull final Request req, @Nonnull final Response res) {
        String msg = String.format("invalid Response! request=%s, response=%s", req, res);
        try {
            final String resBody = RetrofitUtils.readResponseBody(res);
            if (StringUtils.hasText(resBody)) {
                msg += ",body=" + resBody;
            }
        } catch (ReadResponseBodyException e) {
            final String message = String.format("read ResponseBody error! request=%s, response=%s", req, res);
            throw new RetrofitException(message, e);
        } finally {
            res.close();
        }
        return new RetrofitException(msg);
    }

    public static RetrofitException errorExecuting(@Nonnull final Request req, @Nonnull final IOException cause) {
        final String message = cause.getMessage() + ",request=" + req;
        return new RetrofitIOException(message, cause);
    }

    public static RetrofitException errorUnknown(@Nonnull final Request req, @Nonnull final Exception cause) {
        if (cause instanceof RetrofitException) {
            return (RetrofitException) cause;
        }
        final String message = cause.getMessage() + ",request=" + req;
        return new RetrofitException(message, cause);
    }
}
