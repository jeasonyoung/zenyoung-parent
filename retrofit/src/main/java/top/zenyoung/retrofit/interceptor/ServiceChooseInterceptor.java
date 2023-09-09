package top.zenyoung.retrofit.interceptor;

import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import retrofit2.Invocation;
import top.zenyoung.retrofit.annotation.RetrofitClient;
import top.zenyoung.retrofit.ServiceInstanceChooser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Objects;

/**
 * 服务选择拦截器
 *
 * @author young
 */
@RequiredArgsConstructor
public class ServiceChooseInterceptor extends BaseInterceptor implements EnvironmentAware {
    protected final ServiceInstanceChooser serviceInstanceChooser;
    private Environment env;

    @Nonnull
    @Override
    protected Response doIntercept(@Nonnull final Chain chain, @Nonnull final Request req) throws IOException {
        final Method method = Objects.requireNonNull(req.tag(Invocation.class)).method();
        final Class<?> cls = method.getDeclaringClass();
        final RetrofitClient client = AnnotatedElementUtils.findMergedAnnotation(cls, RetrofitClient.class);
        if (Objects.isNull(client) || StringUtils.hasText(client.baseUrl())) {
            return chain.proceed(req);
        }
        //serviceId服务发现
        String serviceId = client.serviceId();
        if (!StringUtils.hasText(serviceId)) {
            return chain.proceed(req);
        }
        if (Objects.nonNull(env)) {
            serviceId = env.resolveRequiredPlaceholders(serviceId);
        }
        final URI uri = serviceInstanceChooser.choose(serviceId);
        final HttpUrl newUrl = req.url().newBuilder()
                .scheme(uri.getScheme())
                .host(uri.getHost())
                .port(uri.getPort())
                .build();
        final Request newReq = req.newBuilder()
                .url(newUrl)
                .build();
        return chain.proceed(newReq);
    }

    @Override
    public void setEnvironment(@Nonnull final Environment env) {
        this.env = env;
    }
}
