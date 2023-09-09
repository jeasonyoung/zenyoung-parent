package top.zenyoung.retrofit.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Invocation;
import top.zenyoung.retrofit.util.AnnotationExtendUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 日志拦截器
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingInterceptor implements Interceptor {
    protected final LogProperty logProperty;

    @Nonnull
    @Override
    public final Response intercept(@Nonnull final Chain chain) throws IOException {
        final Logging logging = findLogging(chain);
        if (!needLog(logging)) {
            return chain.proceed(chain.request());
        }
        final LogLevel level = logging == null ? logProperty.getLogLevel() : logging.logLevel();
        final LogStrategy strategy = logging == null ? logProperty.getLogStrategy() : logging.logStrategy();
        final HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.valueOf(strategy.name());
        return intercept(chain, matchLogger(level), logLevel);
    }

    protected Response intercept(@Nonnull final Chain chain, @Nonnull final HttpLoggingInterceptor.Logger logger,
                                 @Nonnull final HttpLoggingInterceptor.Level logLevel) throws IOException {
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(logger).setLevel(logLevel);
        return loggingInterceptor.intercept(chain);
    }

    protected Logging findLogging(@Nonnull final Chain chain) {
        final Method method = Objects.requireNonNull(chain.request().tag(Invocation.class)).method();
        return AnnotationExtendUtils.findMergedAnnotation(method, method.getDeclaringClass(), Logging.class);
    }

    protected boolean needLog(@Nullable final Logging logging) {
        if (logProperty.isEnable()) {
            if (Objects.isNull(logging)) {
                return true;
            }
            return logging.enable();
        }
        return Objects.nonNull(logging) && logging.enable();
    }

    protected HttpLoggingInterceptor.Logger matchLogger(@Nullable final LogLevel level) {
        if (level == LogLevel.ERROR) {
            return log::error;
        }
        if (level == LogLevel.WARN) {
            return log::warn;
        }
        if (level == LogLevel.INFO) {
            return log::info;
        }
        if (level == LogLevel.DEBUG) {
            return log::debug;
        }
        if (level == LogLevel.TRACE) {
            return log::trace;
        }
        return log::info;
    }
}
