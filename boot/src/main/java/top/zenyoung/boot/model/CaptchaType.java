package top.zenyoung.boot.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.captcha.generator.CodeGenerator;
import top.zenyoung.common.captcha.generator.MathGenerator;
import top.zenyoung.common.captcha.generator.RandomGenerator;
import top.zenyoung.common.model.EnumValue;

/**
 * 验证码类型
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CaptchaType implements EnumValue {
    /**
     * 数学计算类型
     */
    Math(0, "数学计算类型", MathGenerator.class),

    /**
     * 随机字符类型
     */
    Random(1, "随机字符类型", RandomGenerator.class);

    private final int val;
    private final String title;
    private final Class<? extends CodeGenerator> typeClass;
}
