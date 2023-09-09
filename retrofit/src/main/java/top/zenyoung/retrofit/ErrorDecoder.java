package top.zenyoung.retrofit;

import okhttp3.Request;
import okhttp3.Response;
import top.zenyoung.retrofit.exception.RetrofitException;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * 错误解码器
 *
 * <p>
 * 当请求发生异常或者收到无效响应结果的时候,
 * 将HTTP相关信息解码到异常中,无效响应应由业务自己判断
 * </p>
 *
 * @author young
 */
public interface ErrorDecoder {
    /**
     * 当无效响应的时候，将HTTP信息解码到异常中，无效响应由业务自行判断。
     *
     * @param req 请求对象
     * @param res 响应对象
     * @return 异常对象
     */
    default RuntimeException invalidRespDecode(@Nonnull final Request req, @Nonnull final Response res) {
        if (!res.isSuccessful()) {
            throw RetrofitException.errorStatus(req, res);
        }
        return null;
    }

    /**
     * 当请求发生IO异常时，将HTTP信息解码到异常中。
     *
     * @param req   请求对象
     * @param cause IO异常
     * @return 异常对象
     */
    default RuntimeException ioExceptionDecode(@Nonnull final Request req, @Nonnull final IOException cause) {
        return RetrofitException.errorExecuting(req, cause);
    }

    /**
     * 当请求发生除IO异常之外的其它异常时，将HTTP信息解码到异常中。
     *
     * @param req   请求对象
     * @param cause IO异常之异常
     * @return 异常对象
     */
    default RuntimeException exceptionDecode(@Nonnull final Request req, @Nonnull final Exception cause) {
        return RetrofitException.errorUnknown(req, cause);
    }

    /**
     * 默认错误解码器
     */
    class DefaultErrorDecoder implements ErrorDecoder {

    }
}
