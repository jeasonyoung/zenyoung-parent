package top.zenyoung.retrofit.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.Request;
import org.springframework.util.Assert;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import top.zenyoung.retrofit.exception.RetrofitException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 同步调用执行，直接返回 #{@link retrofit2.Response} 对象。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseCallAdapterFactory extends CallAdapter.Factory implements InternalCallAdapterFactory {
    public static final ResponseCallAdapterFactory INSTANCE = new ResponseCallAdapterFactory();

    @Nullable
    @Override
    public CallAdapter<?, ?> get(@Nonnull final Type returnType, @Nonnull final Annotation[] annotations, @Nonnull final Retrofit retrofit) {
        if (Response.class.isAssignableFrom(getRawType(returnType))) {
            return new ResponseCallAdapter<>(returnType);
        }
        return null;
    }

    private static final class ResponseCallAdapter<R> implements CallAdapter<R, Response<R>> {

        private final Type returnType;

        ResponseCallAdapter(Type returnType) {
            this.returnType = returnType;
        }

        @Nonnull
        @Override
        public Type responseType() {
            final ParameterizedType parameterizedType = (ParameterizedType) returnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Assert.notEmpty(actualTypeArguments, "Response must specify generic parameters!");
            return actualTypeArguments[0];
        }

        @Nonnull
        @Override
        public Response<R> adapt(Call<R> call) {
            final Request request = call.request();
            try {
                return call.execute();
            } catch (IOException e) {
                throw Objects.requireNonNull(RetrofitException.errorExecuting(request, e));
            }
        }
    }
}
