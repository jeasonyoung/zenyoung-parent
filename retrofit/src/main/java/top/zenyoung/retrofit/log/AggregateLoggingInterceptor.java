package top.zenyoung.retrofit.log;

import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * 同一个请求的日志聚合在一起打印
 *
 * @author young
 */
public class AggregateLoggingInterceptor extends LoggingInterceptor {

    public AggregateLoggingInterceptor(@Nonnull final LogProperty logProperty) {
        super(logProperty);
    }

    @Override
    protected Response intercept(@Nonnull final Chain chain, @Nonnull final HttpLoggingInterceptor.Logger logger,
                                 @Nonnull final HttpLoggingInterceptor.Level logLevel) throws IOException {
        final BufferLogger bufferLogger = new BufferLogger(logger);
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(bufferLogger).setLevel(logLevel);
        final Response response = interceptor.intercept(chain);
        bufferLogger.flush();
        return response;
    }

    @RequiredArgsConstructor
    private static class BufferLogger implements HttpLoggingInterceptor.Logger {
        private StringBuilder builder = new StringBuilder(System.lineSeparator());
        private final HttpLoggingInterceptor.Logger delegate;

        @Override
        public void log(@Nonnull final String message) {
            builder.append(message).append(System.lineSeparator());
        }

        public void flush() {
            delegate.log(builder.toString());
            builder = new StringBuilder(System.lineSeparator());
        }
    }
}
