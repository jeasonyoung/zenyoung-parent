package top.zenyoung.retrofit.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.CollectionUtils;
import retrofit2.Invocation;
import top.zenyoung.retrofit.exception.RetryFailedException;
import top.zenyoung.retrofit.interceptor.BaseInterceptor;
import top.zenyoung.retrofit.util.AnnotationExtendUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 重试-拦截器
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class RetryInterceptor extends BaseInterceptor {
    private final RetryProperty retryProperty;

    @Nonnull
    @Override
    protected Response doIntercept(@Nonnull final Chain chain, @Nonnull final Request req) throws IOException {
        final Method method = Objects.requireNonNull(req.tag(Invocation.class)).method();
        final Retry retry = AnnotationExtendUtils.findMergedAnnotation(method, method.getDeclaringClass(), Retry.class);
        if (!needRetry(retry)) {
            return chain.proceed(req);
        }
        //重试
        final int maxRetries = retry == null ? retryProperty.getMaxRetries() : retry.maxRetries();
        final int intervalMs = retry == null ? retryProperty.getIntervalMs() : retry.intervalMs();
        final RetryRule[] rules = retry == null ? retryProperty.getRetryRules() : retry.retryRules();
        if (maxRetries > 0 && intervalMs > 0 && rules != null) {
            final Set<RetryRule> ruleSets = Stream.of(rules).filter(Objects::nonNull).collect(Collectors.toSet());
            if (!CollectionUtils.isEmpty(ruleSets)) {
                return retryIntercept(new RetryStrategy(maxRetries, intervalMs), ruleSets, chain);
            }
        }
        return chain.proceed(req);
    }

    protected boolean needRetry(@Nullable final Retry retry) {
        if (retryProperty.isEnable()) {
            if (Objects.isNull(retry)) {
                return true;
            }
            return retry.enable();
        }
        return Objects.nonNull(retry) && retry.enable();
    }

    protected Response retryIntercept(@Nonnull final RetryStrategy strategy, @Nonnull final Set<RetryRule> rules,
                                      @Nonnull final Chain chain) throws IOException {
        final int maxRetries = strategy.getMaxRetries();
        while (true) {
            try {
                final Request req = chain.request();
                final Response res = chain.proceed(req);
                // 如果响应状态码是2xx就不用重试，直接返回
                if (!rules.contains(RetryRule.RES_STATUS_NOT_2XX) || res.isSuccessful()) {
                    return res;
                }
                //检查是否还有重试次数
                if (strategy.shouldRetry()) {
                    log.info("The response fails, retry is performed! The response code is " + res.code());
                    res.close();
                    //重试计数
                    strategy.retry();
                    //继续重试提交
                    continue;
                }
                // 最后一次还没成功，返回最后一次response
                return res;
            } catch (Exception e) {
                if (shouldThrowEx(rules, e)) {
                    throw e;
                }
                if (!strategy.shouldRetry()) {
                    //最后一次还没成功，抛出异常
                    throw new RetryFailedException("Retry Failed: Total " + maxRetries
                            + ",attempts made at interval " + strategy.getIntervalMs() + " ms", e);
                }
                //重试计数
                strategy.retry();
            }
        }
    }

    protected boolean shouldThrowEx(@Nonnull final Set<RetryRule> rules, @Nonnull final Exception e) {
        if (rules.contains(RetryRule.OCCUR_EXP)) {
            return false;
        }
        if (rules.contains(RetryRule.OCCUR_IO_EXP)) {
            return !(e instanceof IOException);
        }
        return true;
    }
}
