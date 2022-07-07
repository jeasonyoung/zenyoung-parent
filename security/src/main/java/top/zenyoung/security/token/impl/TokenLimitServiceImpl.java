package top.zenyoung.security.token.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import top.zenyoung.common.util.BeanCacheUtils;
import top.zenyoung.security.auth.Ticket;
import top.zenyoung.security.token.TokenLimitService;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.util.Constants;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 令牌限制-服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class TokenLimitServiceImpl implements TokenLimitService {
    private final static Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final ApplicationContext context;

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
            final String limitKey = getLimitKey(ticket);
            synchronized (LOCKS.computeIfAbsent(limitKey, k -> new Object())) {
                try {
                    //数据入队
                    BeanCacheUtils.consumer(context, RedissonClient.class, client -> client.getQueue(limitKey).offer(accessToken));
                } catch (Throwable e) {
                    log.warn("limitIn(ticket: {},accessToken: {})-exp: {}", ticket, accessToken, e.getMessage());
                } finally {
                    LOCKS.remove(limitKey);
                }
            }
        }
    }

    @Async
    @Override
    public void limitOut(@Nonnull final Ticket ticket, @Nonnull final Integer maxTokenCount) {
        if (maxTokenCount <= 0) {
            return;
        }
        final String limitKey = getLimitKey(ticket);
        synchronized (LOCKS.computeIfAbsent(limitKey, k -> new Object())) {
            try {
                BeanCacheUtils.consumer(context, RedissonClient.class, client -> {
                    //获取队列
                    final RQueue<String> queue = client.getQueue(limitKey);
                    //队列长度
                    final int size = queue.size();
                    if (size <= maxTokenCount) {
                        return;
                    }
                    //获取对头
                    final String oldToken = queue.poll();
                    //检查对头令牌
                    if (!Strings.isNullOrEmpty(oldToken)) {
                        BeanCacheUtils.consumer(context, TokenService.class, tokenService -> {
                            //检查令牌是否无效
                            final Ticket t = tokenService.validToken(oldToken);
                            //删除令牌
                            tokenService.delToken(oldToken);
                            //检查令牌是否已无效
                            if (Objects.isNull(t)) {
                                //令牌已无效则递归弹出
                                limitOut(ticket, maxTokenCount);
                            }
                        });
                    }
                });
            } catch (Throwable e) {
                log.warn("limitOut(ticket: {},maxTokenCount: {})-exp: {}", ticket, maxTokenCount, e.getMessage());
            } finally {
                LOCKS.remove(limitKey);
            }
        }
    }

    @Async
    @Override
    public void remove(@Nonnull final Ticket ticket, @Nonnull final String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            return;
        }
        final String limitKey = getLimitKey(ticket);
        synchronized (LOCKS.computeIfAbsent(limitKey, k -> new Object())) {
            try {
                BeanCacheUtils.consumer(context, RedissonClient.class, client -> {
                    //获取队列
                    final RQueue<String> queue = client.getQueue(limitKey);
                    //移除数据
                    queue.remove(accessToken);
                });
            } catch (Throwable e) {
                log.warn("remove(ticket: {},accessToken: {})-exp: {}", ticket, accessToken, e.getMessage());
            } finally {
                LOCKS.remove(limitKey);
            }
        }
    }

    @Override
    public boolean isExists(@Nonnull final Ticket ticket, @Nonnull final String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            return false;
        }
        final String limitKey = getLimitKey(ticket);
        synchronized (LOCKS.computeIfAbsent(limitKey, k -> new Object())) {
            try {
                final AtomicBoolean ref = new AtomicBoolean(false);
                BeanCacheUtils.consumer(context, RedissonClient.class, client -> {
                    //获取队列
                    final RQueue<String> queue = client.getQueue(limitKey);
                    //检查是否存在
                    ref.set(queue.contains(accessToken));
                });
                return ref.get();
            } catch (Throwable e) {
                log.warn("isExists(ticket: {},accessToken: {})-exp: {}", ticket, accessToken, e.getMessage());
                return false;
            } finally {
                LOCKS.remove(limitKey);
            }
        }
    }
}
