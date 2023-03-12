package top.zenyoung.graphics.service.impl;

import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.util.CacheUtils;
import top.zenyoung.graphics.service.CaptchaStorageService;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * 验证码存储-服务接口实现(内存)
 *
 * @author young
 */
@RequiredArgsConstructor(staticName = "of")
public class CaptchaStorageMemoryServiceImpl implements CaptchaStorageService {
    private final Cache<Long, Map.Entry<String, Long>> caches = CacheUtils.createCache(100, Duration.ofMinutes(15));

    @Override
    public void addCaptcha(@Nonnull final Long id, @Nonnull final String code, @Nonnull final Duration expire) {
        caches.put(id, Maps.immutableEntry(code, System.currentTimeMillis() + expire.toMillis()));
    }

    @Override
    public String getCaptcha(@Nonnull final Long id) {
        final Map.Entry<String, Long> entry = caches.getIfPresent(id);
        if (Objects.nonNull(entry) && entry.getValue() > System.currentTimeMillis()) {
            return entry.getKey();
        }
        return null;
    }

    @Override
    public void clearCaptcha(@Nonnull final Long id) {
        caches.invalidate(id);
    }
}
