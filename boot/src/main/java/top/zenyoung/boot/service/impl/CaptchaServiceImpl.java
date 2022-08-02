package top.zenyoung.boot.service.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.boot.config.CaptchaProperties;
import top.zenyoung.boot.model.CaptchaCategory;
import top.zenyoung.boot.model.CaptchaType;
import top.zenyoung.boot.service.CaptchaService;
import top.zenyoung.common.captcha.BaseCaptcha;
import top.zenyoung.common.captcha.Captcha;
import top.zenyoung.common.captcha.generator.CodeGenerator;
import top.zenyoung.common.image.ImageUtils;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.Sequence;
import top.zenyoung.common.util.BeanCacheUtils;
import top.zenyoung.common.vo.CaptchaVO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 认证验证码-服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    private static final Map<String, Object> LOCKS = Maps.newConcurrentMap();
    private final CaptchaProperties captchaProperties;
    private final ApplicationContext context;

    private Captcha captcha;

    private StringRedisTemplate redisTemplate;

    public void init() {
        //RedisTemplate
        this.redisTemplate = context.getBean(StringRedisTemplate.class);
        //验证码配置
        if (Objects.nonNull(captchaProperties)) {
            //参数配置
            final Properties props = captchaProperties.getProperties();
            //验证码文字生成器
            final CodeGenerator codeGenerator = createCodeGenerator(captchaProperties.getType(), props);
            //验证码类型
            this.captcha = createCaptcha(captchaProperties.getCategory(), codeGenerator, captchaProperties.getWidth(), captchaProperties.getHeight(), props);
        }
    }

    private Long nextId() {
        final Long id = BeanCacheUtils.function(context, IdSequence.class, Sequence::nextId);
        return Objects.nonNull(id) ? id : System.currentTimeMillis();
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
        final String sep = ":";
        return Joiner.on(sep).skipNulls().join("zy-framework", "captcha", captchaId);
    }


    private void addCaptchaCodeCache(final long captchaId, final String captchaCode, final Duration expire) {
        if (captchaId > 0 && !Strings.isNullOrEmpty(captchaCode)) {
            final String key = getCaptchaCodeKey(captchaId);
            synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
                try {
                    final Duration e = Objects.isNull(expire) ? Duration.ofSeconds(120) : expire;
                    redisTemplate.opsForValue().set(key, captchaCode, e);
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
                    redisTemplate.opsForValue().get(key);
                } finally {
                    LOCKS.remove(key);
                }
            }
        }
        return null;
    }

    private void clearCaptchaCodeCache(final long captchaId) {
        if (captchaId > 0) {
            final String key = getCaptchaCodeKey(captchaId);
            synchronized (LOCKS.computeIfAbsent(key, k -> new Object())) {
                try {
                    redisTemplate.delete(key);
                } finally {
                    LOCKS.remove(key);
                }
            }
        }
    }

    @Override
    public CaptchaVO createCaptcha(final Integer len, final Duration expire) {
        Assert.notNull(this.captcha, "Captcha初始化失败!");
        synchronized (this) {
            final long captchaId = nextId();
            this.captcha.createCode(len);
            final String captchaCode = this.captcha.getCode();
            final String base64Data = this.captcha.getImageBase64Data();
            //缓存数据
            addCaptchaCodeCache(captchaId, captchaCode, expire);
            //返回数据
            return CaptchaVO.of(captchaId, base64Data);
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
                        final boolean ret = this.captcha.verify(captchaCode, inputCode);
                        if (ret) {
                            //验证成功
                            clearCaptchaCodeCache(captchaId);
                        }
                        return ret;
                    }
                } finally {
                    LOCKS.remove(lock);
                }
            }
        }
        return false;
    }
}
