package top.zenyoung.common.captcha.generator;

import top.zenyoung.common.util.RandomUtils;

/**
 * 随机字符验证码生成器<br>
 * 可以通过传入的基础集合和长度随机生成验证码字符
 *
 * @author young
 */
public abstract class BaseGenerator implements CodeGenerator {
    /**
     * 基础字符集合，用于随机获取字符串的字符集合
     */
    protected final String baseStr;
    /**
     * 验证码长度
     */
    protected final int length;

    /**
     * 构造，使用字母+数字做为基础
     *
     * @param count 生成验证码长度
     */
    public BaseGenerator(final int count) {
        this(RandomUtils.BASE_CHAR_NUMBER, count);
    }

    /**
     * 构造
     *
     * @param baseStr 基础字符集合，用于随机获取字符串的字符集合
     * @param length  生成验证码长度
     */
    public BaseGenerator(final String baseStr, final int length) {
        this.baseStr = baseStr;
        this.length = length;
    }

    /**
     * 获取长度验证码
     *
     * @return 验证码长度
     */
    public int getLength() {
        return this.length;
    }
}
