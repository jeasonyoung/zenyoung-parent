package top.zenyoung.framework.runtime.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.common.captcha.BaseCaptcha;
import top.zenyoung.common.captcha.Captcha;
import top.zenyoung.common.captcha.generator.CodeGenerator;
import top.zenyoung.common.image.ImageUtils;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.framework.Constants;
import top.zenyoung.framework.auth.AuthProperties;
import top.zenyoung.framework.captcha.CaptchaCategory;
import top.zenyoung.framework.captcha.CaptchaProperties;
import top.zenyoung.framework.captcha.CaptchaType;
import top.zenyoung.framework.service.AuthCaptchaService;
import top.zenyoung.framework.service.RedisEnhancedService;
import top.zenyoung.framework.utils.BeanCacheUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * 认证验证码-服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class AuthCaptchaServiceImpl implements AuthCaptchaService, InitializingBean {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final AuthProperties authProperties;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationContext context;

    private Captcha captcha;

    private Long nextId() {
        return BeanCacheUtils.function(context, IdSequence.class, Sequence::nextId);
    }

    private void redisHandler(@Nonnull final Runnable handler) {
        BeanCacheUtils.consumer(context, RedisEnhancedService.class, bean -> bean.redisHandler(handler));
    }

    private <T> T redisHandler(@Nonnull final Supplier<T> handler) {
        return BeanCacheUtils.function(context, RedisEnhancedService.class, bean -> bean.redisHandler(handler));
    }

    @Override
    public void afterPropertiesSet() {
        final CaptchaProperties captchaProperties = authProperties.getCaptcha();
        if (Objects.nonNull(captchaProperties)) {
            //参数配置
            final Properties props = captchaProperties.getProperties();
            //验证码文字生成器
            final CodeGenerator codeGenerator = createCodeGenerator(captchaProperties.getType(), props);
            //验证码类型
            this.captcha = createCaptcha(captchaProperties.getCategory(), codeGenerator, captchaProperties.getWidth(), captchaProperties.getHeight(), props);
        }
    }

    private CodeGenerator createCodeGenerator(@Nonnull final CaptchaType type, @Nonnull final Properties props) {
        final Class<? extends CodeGenerator> typeClass;
        if (Objects.nonNull(typeClass = type.getTypeClass())) {
            try {
                if (type == CaptchaType.Math) {
                    //数学计算类型
                    final int numberLength = Integer.parseInt(props.getProperty("numberLength", "1"));
                    return ReflectionUtils.accessibleConstructor(typeClass, int.class).newInstance(numberLength);
                } else {
                    //随机字符类型
                    final int charLength = Integer.parseInt(props.getProperty("charLength", "4"));
                    return ReflectionUtils.accessibleConstructor(typeClass, int.class).newInstance(charLength);
                }
            } catch (Throwable e) {
                log.error("createCodeGenerator(type: {},props: {})-exp: {}", type, props, e.getMessage());
            }
        }
        return null;
    }

    private Captcha createCaptcha(@Nonnull final CaptchaCategory category, @Nullable final CodeGenerator generator,
                                  @Nonnull final Integer width, @Nonnull final Integer height, @Nonnull final Properties props) {
        final Class<? extends Captcha> captchaClass;
        if (Objects.nonNull(captchaClass = category.getCategoryClass())) {
            try {
                final Captcha captcha = ReflectionUtils.accessibleConstructor(captchaClass, int.class, int.class).newInstance(width, height);
                final List<String> excludes = Lists.newArrayList("font", "background", "generator");
                if (captcha instanceof BaseCaptcha) {
                    final BaseCaptcha baseCaptcha = (BaseCaptcha) captcha;
                    if (generator != null) {
                        baseCaptcha.setGenerator(generator);
                    }
                    //字体
                    final String fontName = props.getProperty("font", "Arial");
                    final int fontSize = Integer.parseInt(props.getProperty("font-size", "48"));
                    baseCaptcha.setFont(new Font(fontName, Font.BOLD, fontSize));
                    //背景色
                    final Color background = ImageUtils.getColor(props.getProperty("background", "PINK"));
                    baseCaptcha.setBackground(Objects.isNull(background) ? Color.PINK : background);
                }
                //加载配置处理
                ReflectionUtils.doWithFields(captchaClass, field -> {
                    //检查非静态字段
                    if (!Modifier.isStatic(field.getModifiers())) {
                        final String val = props.getProperty(field.getName());
                        if (!Strings.isNullOrEmpty(val)) {
                            field.setAccessible(true);
                            field.set(captcha, val);
                        }
                    }
                }, field -> !excludes.contains(field.getName()));
                //返回对象
                return captcha;
            } catch (Throwable e) {
                log.error("createCaptcha(category: {},generator: {},width: {},height: {},props: {})-exp: {}", category, generator, width, height, props, e.getMessage());
            }
        }
        return null;
    }

    private String getCaptchaCodeKey(final long captchaId) {
        return Constants.PREFIX + Constants.join("captcha", String.valueOf(captchaId));
    }

    private void addCaptchaCodeCache(final long captchaId, final String captchaCode) {
        if (captchaId > 0 && !Strings.isNullOrEmpty(captchaCode)) {
            final String key = getCaptchaCodeKey(captchaId);
            synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
                try {
                    redisHandler(() -> redisTemplate.opsForValue().set(key, captchaCode, Duration.ofSeconds(120)));
                } finally {
                    LOCKS.remove(key);
                }
            }
        }
    }

    private String getCaptchaCodeCache(final long captchaId) {
        if (captchaId > 0) {
            final String key = getCaptchaCodeKey(captchaId);
            synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
                try {
                    return redisHandler(() -> redisTemplate.opsForValue().get(key));
                } finally {
                    LOCKS.remove(key);
                }
            }
        }
        return null;
    }

    @Override
    public AuthCaptcha createCaptcha() {
        Assert.notNull(this.captcha, "Captcha初始化失败!");
        synchronized (this) {
            final long captchaId = nextId();
            this.captcha.createCode();
            final String captchaCode = this.captcha.getCode();
            final String base64Data = this.captcha.getImageBase64Data();
            //缓存数据
            addCaptchaCodeCache(captchaId, captchaCode);
            //返回数据
            return AuthCaptcha.of(captchaId, base64Data);
        }
    }

    @Override
    public boolean verify(@Nonnull final Long captchaId, @Nonnull final String inputCode) {
        if (captchaId > 0 && !Strings.isNullOrEmpty(inputCode) && Objects.nonNull(this.captcha)) {
            final String lock = "verify-" + captchaId;
            synchronized (LOCKS.computeIfAbsent(lock, k -> new Object())) {
                try {
                    //加载缓存验证码
                    final String captchaCode = getCaptchaCodeCache(captchaId);
                    if (!Strings.isNullOrEmpty(captchaCode)) {
                        return this.captcha.verify(captchaCode, inputCode);
                    }
                } finally {
                    LOCKS.remove(lock);
                }
            }
        }
        return false;
    }
}
