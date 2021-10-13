package top.zenyoung.generator.service.impl;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.generator.model.DatabaseConnect;
import top.zenyoung.generator.service.GeneratorCacheService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * 代码生成-缓存-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
public class GeneratorCacheServiceImpl implements GeneratorCacheService {
    private static final Cache<String, DatabaseConnect> DB_CACHE = CacheUtils.createCache(500, Duration.ofMinutes(120));
    private static final Cache<String, Long> TOKEN_CACHE = CacheUtils.createCache(1000, Duration.ofMinutes(100));
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();

    @Override
    public void putConnect(@Nonnull final String key, @Nonnull final DatabaseConnect config) {
        if (!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(config.getConnectString())) {
            synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
                try {
                    DB_CACHE.put(key, config);
                } finally {
                    LOCKS.remove(key);
                }
            }
        }
    }

    @Override
    public DatabaseConnect getConnect(@Nonnull final String key) {
        if (!Strings.isNullOrEmpty(key)) {
            return DB_CACHE.getIfPresent(key);
        }
        return null;
    }

    @Override
    public String createToken() {
        final String key = this.getClass().getName();
        synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
            try {
                final String token = DigestUtils.sha1Hex(UUID.randomUUID().toString() + System.currentTimeMillis());
                if (!Strings.isNullOrEmpty(token)) {
                    TOKEN_CACHE.put(token, System.currentTimeMillis());
                    return token;
                }
            } finally {
                LOCKS.remove(key);
            }
        }
        return null;
    }

    @Override
    public boolean verifyToken(@Nullable final String token) {
        if (!Strings.isNullOrEmpty(token)) {
            synchronized (LOCKS.computeIfAbsent(token, k -> new Object())) {
                try {
                    final Long stamp = TOKEN_CACHE.getIfPresent(token);
                    return stamp != null && stamp > 0;
                } finally {
                    LOCKS.remove(token);
                }
            }
        }
        return false;
    }
}
