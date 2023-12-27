package top.zenyoung.redis.aop;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import top.zenyoung.boot.annotation.LimitPolicy;
import top.zenyoung.boot.annotation.RateLimiter;
import top.zenyoung.boot.aop.BaseAspect;
import top.zenyoung.boot.util.HttpUtils;
import top.zenyoung.common.exception.ServiceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 请求限流-切面处理
 *
 * @author young
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
public class RateLimiterAspect extends BaseAspect {
    private static final String SEP = "-";
    private static final String RATE_LIMIT_PEFIX = "rate_limit:";

    private final RedissonClient redissonClient;

    @Before("@annotation(rateLimiter)")
    public void doBefore(final JoinPoint joinPoint, final RateLimiter rateLimiter) {
        final int time = rateLimiter.time(), max = rateLimiter.max();
        if (time > 0 && max > 0) {
            final String rateLimiterKey = RATE_LIMIT_PEFIX + getRateLimitKey(joinPoint, rateLimiter);
            final long number = getRateLimiter(rateLimiter.policy(), rateLimiterKey, max, time);
            if (number == -1) {
                throw new ServiceException("访问过于频繁,请稍后再试");
            }
            log.info("限流令牌: {}, 最大令牌数: {}, 剩余令牌: {}", rateLimiterKey, max, number);
        }
    }

    private long getRateLimiter(@Nullable final LimitPolicy policy, @Nonnull final String limitKey, @Nonnull final Integer max, @Nonnull final Integer time) {
        try {
            final RRateLimiter rateLimiter = redissonClient.getRateLimiter(limitKey);
            if (rateLimiter != null) {
                RateType rateType = RateType.OVERALL;
                if (LimitPolicy.USER == policy) {
                    rateType = RateType.PER_CLIENT;
                }
                rateLimiter.trySetRate(rateType, max, time, RateIntervalUnit.SECONDS);
                if (rateLimiter.tryAcquire()) {
                    return rateLimiter.availablePermits();
                }
            }
        } catch (Exception ex) {
            log.error("getRateLimiter(policy: {},limitKey: {},max: {},time: {})-exp: {}", policy, limitKey, max, time, ex.getMessage());
            throw new ServiceException("服务器限流异常:" + ex.getMessage());
        }
        return -1L;
    }


    private String getRateLimitKey(@Nonnull final JoinPoint joinPoint, @Nonnull final RateLimiter rateLimiter) {
        final StringBuilder builder = new StringBuilder();
        //限流键
        if (!Strings.isNullOrEmpty(rateLimiter.key())) {
            builder.append(rateLimiter.key()).append(SEP);
        }
        //限流策略
        final LimitPolicy policy = rateLimiter.policy();
        if (LimitPolicy.IP == policy) {
            //IP
            final String ipAddr = HttpUtils.getClientIpAddr();
            if (!Strings.isNullOrEmpty(ipAddr)) {
                builder.append(ipAddr).append(SEP);
            }
        } else if (LimitPolicy.USER == policy) {
            //用户标识
            final HttpServletRequest request = HttpUtils.getWebRequest();
            if (request != null) {
                final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (!Strings.isNullOrEmpty(token)) {
                    builder.append(token).append(SEP);
                } else {
                    builder.append(redissonClient.getId()).append(SEP);
                }
            }
        }
        //限流拦截实现
        final Signature signature = joinPoint.getSignature();
        if (signature != null) {
            builder.append(signature.getDeclaringTypeName()).append(".").append(signature.getName());
        }
        return DigestUtils.md5Hex(builder.toString());
    }
}
