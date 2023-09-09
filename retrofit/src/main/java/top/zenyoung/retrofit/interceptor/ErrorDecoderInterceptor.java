package top.zenyoung.retrofit.interceptor;

import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import retrofit2.Invocation;
import top.zenyoung.retrofit.ErrorDecoder;
import top.zenyoung.retrofit.annotation.RetrofitClient;
import top.zenyoung.retrofit.util.ContextUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 错误解码拦截器
 *
 * @author young
 */
public class ErrorDecoderInterceptor extends BaseInterceptor implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Nonnull
    @Override
    protected Response doIntercept(@Nonnull final Chain chain, @Nonnull final Request req) throws IOException {
        final Method method = Objects.requireNonNull(req.tag(Invocation.class)).method();
        final RetrofitClient client = AnnotatedElementUtils.findMergedAnnotation(
                method.getDeclaringClass(), RetrofitClient.class
        );
        if (Objects.isNull(client)) {
            return chain.proceed(req);
        }
        final ErrorDecoder errorDecoder = ContextUtils.getBeanOrNew(context, client.errorDecoder());
        boolean decoded = false;
        try {
            final Response res = chain.proceed(req);
            if (Objects.isNull(errorDecoder)) {
                return res;
            }
            decoded = true;
            final Exception exp = errorDecoder.invalidRespDecode(req, res);
            if (Objects.isNull(exp)) {
                return res;
            }
            throw exp;
        } catch (IOException e) {
            if (decoded) {
                throw e;
            }
            throw errorDecoder.ioExceptionDecode(req, e);
        } catch (Exception e) {
            if (decoded && (e instanceof RuntimeException)) {
                throw (RuntimeException) e;
            }
            throw errorDecoder.exceptionDecode(req, e);
        }
    }
}
