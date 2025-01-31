package top.zenyoung.redis.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import top.zenyoung.boot.annotation.RepeatSubmit;
import top.zenyoung.boot.aop.BaseAspect;
import top.zenyoung.boot.config.RepeatSubmitProperties;
import top.zenyoung.common.exception.ServiceException;
import top.zenyoung.boot.util.HttpUtils;
import top.zenyoung.common.util.JsonUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交-切面
 *
 * @author young
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
public class RepeatSubmitAspect extends BaseAspect {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private static final String REPEAT_SUBMIT_PEFIX = "zy:repeat-submit:";
    private final RepeatSubmitProperties properties;

    private final RedissonClient redissonClient;
    private final ObjectMapper objMapper;

    @Before("@annotation(repeatSubmit)")
    public void doBefore(final JoinPoint joinPoint, final RepeatSubmit repeatSubmit) {
        //检查是否启用
        if (Objects.isNull(properties) || !properties.isEnabled()) {
            return;
        }
        //启用处理
        final Duration g = properties.getInterval();
        final Duration d = Strings.isNullOrEmpty(repeatSubmit.duration()) ? g : Duration.parse(repeatSubmit.duration());
        final Duration interval = Duration.ofMillis(Math.max(g.toMillis(), d.toMillis()));
        if (interval == null || interval.getSeconds() <= 0) {
            throw new ServiceException("重复提交间隔时间不能小于'1'秒:" + interval);
        }
        //当前参数字符串
        final String params = Joiner.on(",").join(getReqArgs(joinPoint, arg -> JsonUtils.toJson(objMapper, arg)));
        //当前请求
        final HttpServletRequest request = HttpUtils.getWebRequest();
        if (request == null) {
            throw new ServiceException("获取当前请求失败!");
        }
        //构建请求标识
        final String submitKey = DigestUtils.md5Hex(Joiner.on("|").skipNulls().join(new String[]{
                request.getRequestURI(),
                request.getHeader(HttpHeaders.AUTHORIZATION),
                params
        }));
        //唯一标识(指定key + 消息头)
        final String cacheRepeatKey = REPEAT_SUBMIT_PEFIX + submitKey;
        synchronized (LOCKS.computeIfAbsent(cacheRepeatKey, k -> new Object())) {
            try {
                final long time = interval.toMillis(), wait = time * 10;
                //分布式锁处理
                final RLock lock = redissonClient.getLock(cacheRepeatKey);
                if (lock == null) {
                    throw new ServiceException(repeatSubmit.message());
                }
                //获取锁处理
                if (!lock.tryLock(wait, time, TimeUnit.MILLISECONDS)) {
                    throw new ServiceException(repeatSubmit.message());
                }
            } catch (InterruptedException ex) {
                log.warn("doBefore(joinPoint: {},repeatSubmit: {})[cacheRepeatKey: {}]-exp: {}", joinPoint, repeatSubmit, cacheRepeatKey, ex.getMessage());
                throw new ServiceException(repeatSubmit.message() + ":" + ex.getMessage());
            } finally {
                LOCKS.remove(cacheRepeatKey);
            }
        }
    }

}
