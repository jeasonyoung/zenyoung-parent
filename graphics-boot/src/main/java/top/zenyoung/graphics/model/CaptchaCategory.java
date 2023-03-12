package top.zenyoung.graphics.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.zenyoung.common.model.EnumValue;
import top.zenyoung.graphics.captcha.*;

/**
 * 验证码类别
 *
 * @author young
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CaptchaCategory implements EnumValue {
    /**
     * 干扰线图形验证码
     */
    Line(0, "干扰线图形验证码", LineCaptcha.class),
    /**
     * 圆圈干扰图形验证码
     */
    Circle(1, "圆圈干扰图形验证码", CircleCaptcha.class),
    /**
     * 扭曲干扰图形验证码
     */
    Shear(2, "扭曲干扰图形验证码", ShearCaptcha.class),
    /**
     * Gif动画图形验证码
     */
    Gif(3, "Gif动画图形验证码", GifCaptcha.class);

    private final int val;
    private final String title;
    private final Class<? extends Captcha> categoryClass;
}
