package top.zenyoung.generator.service.impl;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.zenyoung.generator.model.DatabaseConnect;
import top.zenyoung.generator.service.GeneratorConnectCacheService;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * 数据库链接缓存-服务接口实现
 *
 * @author young
 */
@Slf4j
@Service
public class GeneratorConnectCacheServiceImpl implements GeneratorConnectCacheService {
    private static final Cache<String, DatabaseConnect> CACHE = CacheBuilder.newBuilder().maximumSize(50).expireAfterAccess(Duration.ofMinutes(120)).build();

    @Override
    public void putConnect(@Nonnull final String key, @Nonnull final DatabaseConnect config) {
        if (!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(config.getConnectString())) {
            CACHE.put(key, config);
        }
    }

    @Override
    public DatabaseConnect getConnect(@Nonnull final String key) {
        if (!Strings.isNullOrEmpty(key)) {
            return CACHE.getIfPresent(key);
        }
        return null;
    }
}
