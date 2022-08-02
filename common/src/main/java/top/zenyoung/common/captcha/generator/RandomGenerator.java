package top.zenyoung.common.captcha.generator;

import com.google.common.base.Strings;
import top.zenyoung.common.util.RandomUtils;

import java.util.Objects;

/**
 * 随机字符验证码生成器<br>
 * 可以通过传入的基础集合和长度随机生成验证码字符
 *
 * @author young
 */
public class RandomGenerator extends BaseGenerator {

    /**
     * 构造，使用字母+数字做为基础
     *
     * @param len 生成验证码长度
     */
    public RandomGenerator(final int len) {
        super(len);
    }

    /**
     * 构造
     *
     * @param baseStr 基础字符集合，用于随机获取字符串的字符集合
     * @param length  生成验证码长度
     */
    public RandomGenerator(final String baseStr, final int length) {
        super(baseStr, length);
    }

    @Override
    public String generate(final Integer len) {
        if (Objects.nonNull(len) && len > 0) {
            this.setLen(len);
        }
        return RandomUtils.randomString(this.getBaseStr(), this.getLen());
    }

    @Override
    public boolean verify(final String captchaCode, final String inputCode) {
        return !Strings.isNullOrEmpty(captchaCode) && !Strings.isNullOrEmpty(inputCode) && captchaCode.equalsIgnoreCase(inputCode);
    }
}
