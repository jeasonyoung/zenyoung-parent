package top.zenyoung.graphics.captcha.generator;

import com.google.common.base.Strings;
import lombok.Data;
import top.zenyoung.common.util.RandomUtils;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;

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
    protected BaseGenerator(final int len) {
        this("0123456789abcdefghijklmnopqrstuvwxyz", len);
    }

    /**
     * 构造
     *
     * @param baseStr 基础字符集合，用于随机获取字符串的字符集合
     * @param length  生成验证码长度
     */
    protected BaseGenerator(final String baseStr, final int length) {
        this.baseStr = baseStr;
        this.len = length;
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     * @see Random#nextInt(int)
     */
    protected final int randomInt(final int limit) {
        return RandomUtils.randomInt(0, limit);
    }

    /**
     * 随机字符
     *
     * @param baseString 随机字符选取的样本
     * @return 随机字符
     * @since 3.1.2
     */
    protected final char randomChar(@Nonnull final String baseString) {
        final int limit = Optional.of(baseString.length()).orElse(1);
        final int idx = RandomUtils.randomInt(0, limit);
        return baseString.charAt(idx);
    }

    /**
     * 获得一个随机的字符串
     *
     * @param baseString 随机字符选取的样本
     * @param length     字符串的长度
     * @return 随机字符串
     */
    protected final String randomString(@Nonnull final String baseString, final int length) {
        if (Strings.isNullOrEmpty(baseString)) {
            return "";
        }
        final int mLen = Math.max(length, 1), baseLength = baseString.length();
        final StringBuilder sb = new StringBuilder(mLen);
        for (int i = 0; i < mLen; i++) {
            final int number = randomInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }
}
