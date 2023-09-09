package top.zenyoung.retrofit.interceptor;

import com.google.common.base.Strings;
import lombok.Setter;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * 拦截器基类
 *
 * @author young
 */
@Setter
public abstract class BaseInterceptor implements Interceptor {
    protected String[] includes;
    protected String[] excludes;
    protected final PathMatcher pathMatcher = new AntPathMatcher();

    @Nonnull
    @Override
    public final Response intercept(@Nonnull final Chain chain) throws IOException {
        final Request req = chain.request();
        final String path = req.url().encodedPath();
        if (isMatch(excludes, path)) {
            return chain.proceed(req);
        }
        if (!isMatch(includes, path)) {
            return chain.proceed(req);
        }
        return doIntercept(chain, req);
    }

    private boolean isMatch(@Nullable final String[] patterns, @Nonnull final String path) {
        if (patterns == null) {
            return false;
        }
        for (final String pattern : patterns) {
            if (!Strings.isNullOrEmpty(pattern) && pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    protected abstract Response doIntercept(@Nonnull final Chain chain, @Nonnull final Request req) throws IOException;
}
