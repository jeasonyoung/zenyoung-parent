package top.zenyoung.redis.sync;

import com.alicp.jetcache.support.CacheMessage;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Redis订阅/发布处理器
 *
 * @author young
 */
public class RedisPublisher {
    private static final String ISSUE = DigestUtils.md5DigestAsHex((System.currentTimeMillis() + "," + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
    /**
     * topic前缀
     */
    @Value("${spring.application.name}")
    private String topicPrefix;

    /**
     * Redis模板
     */
    @Autowired
    private RedissonClient client;

    /**
     * 获取Topic
     *
     * @return topic
     */
    public String getTopic() {
        return "topic:" + this.topicPrefix + ":cache";
    }

    /**
     * 发布缓存消息
     *
     * @param area         缓存区域
     * @param cacheName    缓存名称
     * @param cacheMessage 缓存消息
     */
    protected void send(@Nonnull final String area, @Nonnull final String cacheName, @Nonnull final CacheMessage cacheMessage) {
        final LocalCacheEntity entity = LocalCacheEntity.of(ISSUE, area, cacheName, cacheMessage);
        final RTopic topic = this.client.getTopic(getTopic());
        topic.publish(entity);
    }

    /**
     * 订阅缓存通知消息
     *
     * @param handler 消息处理
     */
    protected int subscribe(@Nullable final Consumer<LocalCacheEntity> handler) {
        final String tc = this.getTopic();
        final RTopic topic = this.client.getTopic(tc);
        return topic.addListener(LocalCacheEntity.class, (channel, entity) -> {
            if (tc.equalsIgnoreCase(channel.toString()) && Objects.nonNull(handler)) {
                handler.accept(entity);
            }
        });
    }

    /**
     * 取消订阅
     *
     * @param listenerId 监听器ID
     */
    protected void unsubscribe(final int listenerId) {
        if (listenerId > 0) {
            final RTopic topic = this.client.getTopic(getTopic());
            topic.removeListener(listenerId);
        }
    }

    protected boolean checkIssue(@Nonnull final LocalCacheEntity entity) {
        return ISSUE.equalsIgnoreCase(entity.getIssueId());
    }
}
