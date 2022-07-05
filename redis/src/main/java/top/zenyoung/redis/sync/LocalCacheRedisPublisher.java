package top.zenyoung.redis.sync;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.support.CacheMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import top.zenyoung.redis.jetcache.RedissonDataCacheConfig;

import java.util.Objects;

/**
 * 本地消息发布处理
 *
 * @author young
 */
@Slf4j
@Primary
@Component
public class LocalCacheRedisPublisher extends RedisPublisher implements CacheMessagePublisher {
    @Autowired
    private ConfigProvider configProvider;

    @Override
    public void publish(final String area, final String cacheName, final CacheMessage cacheMessage) {
        try {
            final int type = cacheMessage.getType();
            //缓存写入不发送消息
            if (type == CacheMessage.TYPE_PUT || type == CacheMessage.TYPE_PUT_ALL) {
                return;
            }
            final Cache<?, ?> cache = this.configProvider.getCacheManager().getCache(area, cacheName);
            if (Objects.isNull(cache)) {
                return;
            }
            //远程缓存不处理
            if (cache.config() instanceof RedissonDataCacheConfig) {
                return;
            }
            //发布订阅消息
            this.send(area, cacheName, cacheMessage);
        } catch (Throwable ex) {
            log.warn("publish(area: {},cacheName: {})-exp: {}", area, cacheName, ex.getMessage());
        }
    }
}
