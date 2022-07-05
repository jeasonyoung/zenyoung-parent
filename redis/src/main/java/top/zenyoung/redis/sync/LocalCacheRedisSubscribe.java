package top.zenyoung.redis.sync;

import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.support.CacheMessage;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 本地消息订阅处理
 *
 * @author young
 */
@Slf4j
@Component
public class LocalCacheRedisSubscribe extends RedisPublisher implements InitializingBean, DisposableBean, Consumer<LocalCacheEntity> {
    private final AtomicInteger refSubId = new AtomicInteger(-1);

    @Autowired
    private ConfigProvider provider;

    @Override
    public void afterPropertiesSet() {
        final int ret = this.subscribe(this);
        this.refSubId.set(ret);
        log.info("开始订阅redisson-mq=> {}", ret);
    }

    @Override
    public void destroy() {
        final int subId;
        if ((subId = refSubId.get()) > 0) {
            this.unsubscribe(subId);
            log.info("取消订阅redisson-mq=> {}", subId);
        }
    }

    @Override
    public void accept(final LocalCacheEntity entity) {
        if (Objects.nonNull(entity) && !this.checkIssue(entity)) {
            this.cacheHandler(entity);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void cacheHandler(@Nonnull final LocalCacheEntity entity) {
        final Cache<?, ?> local = (Cache<?, ?>) provider.getCacheManager().getCache(entity.getArea(), entity.getCacheName()).unwrap(Cache.class);
        if (Objects.isNull(local)) {
            return;
        }
        final CacheMessage cm = entity.getCacheMessage();
        if (Objects.nonNull(cm)) {
            switch (cm.getType()) {
                //部分缓存失效
                case CacheMessage.TYPE_REMOVE: {
                    Stream.of(cm.getKeys())
                            .filter(Objects::nonNull)
                            .forEach(key -> {
                                try {
                                    if (Objects.nonNull(local.getIfPresent(key))) {
                                        local.invalidate(key);
                                    }
                                } catch (Throwable ex) {
                                    log.warn("cacheHandler-缓存[{}]失效异常-exp: {}", key, ex.getMessage());
                                }
                            });
                    break;
                }
                case CacheMessage.TYPE_REMOVE_ALL: {
                    try {
                        //缓存失效
                        local.invalidateAll();
                    } catch (Throwable ex) {
                        log.warn("cacheHandler-全部缓存失效异常: {}", ex.getMessage());
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }
}
