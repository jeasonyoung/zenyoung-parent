package top.zenyoung.common.captcha.generator;

import lombok.Data;
import top.zenyoung.common.util.RandomUtils;

/**
 * 随机字符验证码生成器<br>
 * 可以通过传入的基础集合和长度随机生成验证码字符
 *
 * @author young
 */
@Data
public abstract class BaseGenerator implements CodeGenerator {
    /**
     * 基础字符集合，用于随机获取字符串的字符集合
     */
    private final String baseStr;
    /**
     * 验证码长度
     */
    private int len;

    /**
     * 构造，使用字母+数字做为基础
     *
     * @param len 生成验证码长度
     */
    public BaseGenerator(final int len) {
        this(RandomUtils.BASE_CHAR_NUMBER, len);
    }

    /**
     * 构造
     *
     * @param baseStr 基础字符集合，用于随机获取字符串的字符集合
     * @param length  生成验证码长度
     */
    public BaseGenerator(final String baseStr, final int length) {
        this.baseStr = baseStr;
        this.len = length;
    }
}
