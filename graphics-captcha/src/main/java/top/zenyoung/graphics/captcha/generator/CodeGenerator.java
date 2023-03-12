package top.zenyoung.graphics.captcha.generator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 验证码文字生成器
 *
 * @author young
 */
public interface CodeGenerator {
    /**
     * 生成验证码
     *
     * @param len 验证码长度
     * @return 验证码
     */
    String generate(@Nullable final Integer len);

    /**
     * 验证用户输入的字符串是否与生成的验证码匹配<br>
     * 用户通过实现此方法定义验证码匹配方式
     *
     * @param captchaCode 生成的随机验证码
     * @param inputCode   用户输入的验证码
     * @return 是否验证通过
     */
    boolean verify(@Nonnull final String captchaCode, @Nonnull final String inputCode);
}
