package top.zenyoung.retrofit.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.*;
import top.zenyoung.retrofit.exception.RetrofitException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 同步调用,
 * 如果返回的http状态码是是成功,
 * 返回responseBody 反序列化之后的对象
 * 否则,抛出异常
 * 异常信息中包含请求和响应相关信息。
 *
 * @author young
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BodyCallAdapterFactory extends CallAdapter.Factory implements InternalCallAdapterFactory {
    public static final BodyCallAdapterFactory INSTANCE = new BodyCallAdapterFactory();

    @Nullable
    @Override
    public CallAdapter<?, ?> get(@Nonnull final Type returnType, @Nonnull final Annotation[] annotations, @Nonnull final Retrofit retrofit) {
        if (Call.class.isAssignableFrom(getRawType(returnType))) {
            return null;
        }
        if (CompletableFuture.class.isAssignableFrom(getRawType(returnType))) {
            return null;
        }
        if (Response.class.isAssignableFrom(getRawType(returnType))) {
            return null;
        }
        return new BodyCallAdapter<>(returnType, annotations, retrofit);
    }

    @RequiredArgsConstructor
    private static final class BodyCallAdapter<R> implements CallAdapter<R, R> {
        private final Type returnType;
        private final Annotation[] annotations;
        private final Retrofit retrofit;

        @Nonnull
        @Override
        public Type responseType() {
            return returnType;
        }

        @Override
        public R adapt(final Call<R> call) {
            Response<R> res;
            final Request req = call.request();
            try {
                res = call.execute();
            } catch (IOException e) {
                throw Objects.requireNonNull(RetrofitException.errorExecuting(req, e));
            }
            if (res.isSuccessful()) {
                return res.body();
            }
            try (final ResponseBody errorBody = res.errorBody()) {
                if (errorBody == null) {
                    return null;
                }
                final Converter<ResponseBody, R> converter = retrofit.responseBodyConverter(responseType(), annotations);
                try {
                    return converter.convert(Objects.requireNonNull(errorBody));
                } catch (IOException e) {
                    throw Objects.requireNonNull(RetrofitException.errorExecuting(req, e));
                }
            }
        }
    }
}
