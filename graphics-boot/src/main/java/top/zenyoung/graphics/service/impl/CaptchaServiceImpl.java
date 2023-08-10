package top.zenyoung.graphics.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.vo.CaptchaVO;
import top.zenyoung.graphics.captcha.BaseCaptcha;
import top.zenyoung.graphics.captcha.Captcha;
import top.zenyoung.graphics.captcha.generator.CodeGenerator;
import top.zenyoung.graphics.config.GraphicsProperties;
import top.zenyoung.graphics.model.CaptchaCategory;
import top.zenyoung.graphics.model.CaptchaType;
import top.zenyoung.graphics.service.CaptchaService;
import top.zenyoung.graphics.service.CaptchaStorageService;
import top.zenyoung.graphics.util.ImageUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.List;
import java.util.*;

/**
 * 认证验证码-服务接口实现
 *
 * @author young
 */
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class CaptchaServiceImpl implements CaptchaService {
    private static final Map<Long, Object> LOCKS = Maps.newConcurrentMap();
    private final ApplicationContext context;
    private final GraphicsProperties.Captcha captchaProperties;

    private Captcha captcha;
    private CaptchaStorageService storageService;

    public CaptchaService init() {
        //存储服务
        this.storageService = context.getBean(CaptchaStorageService.class);
        //验证码配置
        if (Objects.nonNull(captchaProperties)) {
            //参数配置
            final Properties props = captchaProperties.getProperties();
            //验证码文字生成器
            final CaptchaType type = parseEnumByName(captchaProperties.getType(), CaptchaType.class, CaptchaType.MATH);
            final CodeGenerator codeGenerator = createCodeGenerator(type, props);
            //验证码类型
            final CaptchaCategory category = parseEnumByName(captchaProperties.getCategory(), CaptchaCategory.class, CaptchaCategory.GIF);
            this.captcha = createCaptcha(category, codeGenerator, captchaProperties.getWidth(), captchaProperties.getHeight(), props);
        }
        return this;
    }

    private static <T extends Enum<T>> T parseEnumByName(@Nullable final String name, @Nonnull final Class<T> cls, @Nonnull final T defVal) {
        T val = null;
        if (!Strings.isNullOrEmpty(name)) {
            try {
                val = Enum.valueOf(cls, name.toUpperCase());
            } catch (RuntimeException e) {
                log.error("parseEnumByName(name: {},cls: {})-exp: {}", name, cls, e.getMessage());
            }
        }
        return Optional.ofNullable(val).orElse(defVal);
    }

    private Long nextId() {
        final IdSequence seq = context.getBean(IdSequence.class);
        final Long id = seq.nextId();
        return Objects.nonNull(id) ? id : System.currentTimeMillis();
    }

    private CodeGenerator createCodeGenerator(@Nonnull final CaptchaType type, @Nonnull final Properties props) {
        final Class<? extends CodeGenerator> typeClass;
        if (Objects.nonNull(typeClass = type.getTypeClass())) {
            try {
                if (type == CaptchaType.MATH) {
                    //数学计算类型
                    final int numberLength = Integer.parseInt(props.getProperty("numberLength", "1"));
                    return ReflectionUtils.accessibleConstructor(typeClass, int.class).newInstance(numberLength);
                } else {
                    //随机字符类型
                    final int charLength = Integer.parseInt(props.getProperty("charLength", "4"));
                    return ReflectionUtils.accessibleConstructor(typeClass, int.class).newInstance(charLength);
                }
            } catch (Exception e) {
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
                final Captcha c = ReflectionUtils.accessibleConstructor(captchaClass, int.class, int.class).newInstance(width, height);
                final List<String> excludes = Lists.newArrayList("font", "background", "generator");
                if (c instanceof BaseCaptcha) {
                    final BaseCaptcha baseCaptcha = (BaseCaptcha) c;
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
                return c;
            } catch (Exception e) {
                log.error("createCaptcha(category: {},generator: {},width: {},height: {},props: {})-exp: {}",
                        category, generator, width, height, props, e.getMessage());
            }
        }
        return null;
    }

    private void addCaptchaCodeCache(final long captchaId, final String captchaCode, final Duration expire) {
        if (captchaId > 0 && !Strings.isNullOrEmpty(captchaCode)) {
            synchronized (LOCKS.computeIfAbsent(captchaId, k -> new Object())) {
                try {
                    final Duration e = Objects.isNull(expire) ? Duration.ofSeconds(120) : expire;
                    storageService.addCaptcha(captchaId, captchaCode, e);
                } finally {
                    LOCKS.remove(captchaId);
                }
            }
        }
    }

    private String getCaptchaCodeCache(final long captchaId) {
        if (captchaId > 0) {
            synchronized (LOCKS.computeIfAbsent(captchaId, k -> new Object())) {
                try {
                    return storageService.getCaptcha(captchaId);
                } finally {
                    LOCKS.remove(captchaId);
                }
            }
        }
        return null;
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
            synchronized (LOCKS.computeIfAbsent(captchaId, k -> new Object())) {
                try {
                    //加载缓存验证码
                    final String captchaCode = getCaptchaCodeCache(captchaId);
                    if (!Strings.isNullOrEmpty(captchaCode)) {
                        final boolean ret = this.captcha.verify(captchaCode, inputCode);
                        if (ret) {
                            //验证成功
                            storageService.clearCaptcha(captchaId);
                        }
                        return ret;
                    }
                } finally {
                    LOCKS.remove(captchaId);
                }
            }
        }
        return false;
    }
}
