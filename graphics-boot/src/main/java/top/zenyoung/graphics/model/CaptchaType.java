package top.zenyoung.graphics.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.graphics.captcha.generator.CodeGenerator;
import top.zenyoung.graphics.captcha.generator.MathGenerator;
import top.zenyoung.graphics.captcha.generator.RandomGenerator;

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
    MATH(0, "数学计算类型", MathGenerator.class),
    /**
     * 随机字符类型
     */
    RANDOM(1, "随机字符类型", RandomGenerator.class);

    private final int val;
    private final String title;
    private final Class<? extends CodeGenerator> typeClass;
}
