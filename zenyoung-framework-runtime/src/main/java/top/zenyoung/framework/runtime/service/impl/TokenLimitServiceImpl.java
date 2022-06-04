package top.zenyoung.framework.runtime.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;
import top.zenyoung.common.util.LocalSyncUtils;
import top.zenyoung.framework.Constants;
import top.zenyoung.framework.service.RedisEnhancedService;
import top.zenyoung.framework.utils.BeanCacheUtils;
import top.zenyoung.security.token.Ticket;
import top.zenyoung.security.token.TokenLimitService;
import top.zenyoung.security.token.TokenService;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 令牌限制-服务接口实现
 *
 * @author young
 */
@RequiredArgsConstructor
public class TokenLimitServiceImpl implements TokenLimitService {
    private final static Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final StringRedisTemplate redisTemplate;
    private final ApplicationContext context;

    private void redisHandler(@Nonnull final Runnable handler) {
        BeanCacheUtils.consumer(context, RedisEnhancedService.class, bean -> bean.redisHandler(handler));
    }

    private <T> T redisHandler(@Nonnull final Supplier<T> handler) {
        return BeanCacheUtils.function(context, RedisEnhancedService.class, bean -> bean.redisHandler(handler));
    }

    @Async
    @Override
    public void limit(@Nonnull final Ticket ticket, @Nonnull final Integer maxTokenCount, @Nonnull final String accessToken) {
        limitIn(ticket, accessToken);
        limitOut(ticket, maxTokenCount);
    }

    private String getLimitKey(@Nonnull final Ticket ticket) {
        return Constants.PREFIX + Constants.join("auth-token-limit-key", ticket.getId());
    }

    @Async
    @Override
    public void limitIn(@Nonnull final Ticket ticket, @Nonnull final String accessToken) {
        if (!Strings.isNullOrEmpty(accessToken)) {
            redisHandler(() -> {
                //入队
                final String limitKey = getLimitKey(ticket);
                LocalSyncUtils.syncHandler(LOCKS, limitKey, () -> redisTemplate.opsForList().rightPush(limitKey, accessToken));
            });
        }
    }

    @Async
    @Override
    public void limitOut(@Nonnull final Ticket ticket, @Nonnull final Integer maxTokenCount) {
        if (maxTokenCount <= 0) {
            return;
        }
        redisHandler(() -> {
            final String limitKey = getLimitKey(ticket);
            //获取对头令牌
            final String oldToken = LocalSyncUtils.syncHandler(LOCKS, limitKey, () -> {
                final ListOperations<String, String> queue = redisTemplate.opsForList();
                final Long size = queue.size(limitKey);
                if (Objects.isNull(size) || size <= maxTokenCount) {
                    return null;
                }
                return queue.leftPop(limitKey);
            });
            //检查对头令牌
            if (!Strings.isNullOrEmpty(oldToken)) {
                final TokenService tokenService = context.getBean(TokenService.class);
                //检查令牌是否无效
                final Ticket t = tokenService.validToken(oldToken);
                //删除令牌
                tokenService.delToken(oldToken);
                //检查令牌是否已无效
                if (Objects.isNull(t)) {
                    //令牌已无效则递归弹出
                    limitOut(ticket, maxTokenCount);
                }
            }
        });
    }

    @Async
    @Override
    public void remove(@Nonnull final Ticket ticket, @Nonnull final String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            return;
        }
        redisHandler(() -> {
            final String limitKey = getLimitKey(ticket);
            LocalSyncUtils.syncHandler(LOCKS, limitKey, () -> redisTemplate.opsForList().remove(limitKey, 1, accessToken));
        });
    }

    @Override
    public boolean isExists(@Nonnull final Ticket ticket, @Nonnull final String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            return false;
        }
        return redisHandler(() -> {
            final String limitKey = getLimitKey(ticket);
            return LocalSyncUtils.syncHandler(LOCKS, limitKey, () -> {
                final List<String> tokens = redisTemplate.opsForList().range(limitKey, 0, -1);
                return !CollectionUtils.isEmpty(tokens) && tokens.contains(accessToken);
            });
        });
    }
}
