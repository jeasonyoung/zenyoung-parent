package top.zenyoung.common.captcha.generator;

import com.google.common.base.Strings;
import top.zenyoung.common.util.RandomUtils;

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
     * @param count 生成验证码长度
     */
    public RandomGenerator(final int count) {
        super(count);
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
    public String generate() {
        return RandomUtils.randomString(this.baseStr, this.length);
    }

    @Override
    public boolean verify(final String captchaCode, final String inputCode) {
        return !Strings.isNullOrEmpty(captchaCode) && !Strings.isNullOrEmpty(inputCode) && captchaCode.equalsIgnoreCase(inputCode);
    }
}
