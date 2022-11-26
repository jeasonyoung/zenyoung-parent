package top.zenyoung.boot.service.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.zenyoung.boot.service.CaptchaStorageService;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * 验证码存储-Redis存储
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class RedisCaptchaStorageServiceImpl extends BaseServiceImpl implements CaptchaStorageService {
    private final static String SEP = ":";
    private final StringRedisTemplate redisTemplate;

    private static String getCaptchaCodeKey(final long captchaId) {
        return Joiner.on(SEP).skipNulls().join("zy-framework", "captcha", captchaId);
    }

    @Override
    public void addCaptcha(@Nonnull final Long id, @Nonnull final String code, @Nonnull final Duration expire) {
        if (!Strings.isNullOrEmpty(code)) {
            final String key = getCaptchaCodeKey(id);
            redisTemplate.opsForValue().set(key, code, expire);
        }
    }

    @Override
    public String getCaptcha(@Nonnull final Long id) {
        final String key = getCaptchaCodeKey(id);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void clearCaptcha(@Nonnull final Long id) {
        final String key = getCaptchaCodeKey(id);
        redisTemplate.delete(key);
    }
}
