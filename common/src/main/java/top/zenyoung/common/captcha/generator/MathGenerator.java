package top.zenyoung.common.captcha.generator;

import com.google.common.base.Strings;
import top.zenyoung.common.captcha.Calculator;
import top.zenyoung.common.util.RandomUtils;

import java.util.Objects;

/**
 * 数字计算验证码生成器
 *
 * @author young
 */
public class MathGenerator extends BaseGenerator {
    private static final String OPERATORS = "+-*";

    /**
     * 参与计算数字最大长度
     */
    private int numberLength;

    /**
     * 构造
     */
    public MathGenerator() {
        this(2);
    }

    /**
     * 构造
     *
     * @param numberLength 参与计算最大数字位数
     */
    public MathGenerator(final int numberLength) {
        super(2);
        this.numberLength = numberLength;
    }

    @Override
    public String generate(final Integer len) {
        if (Objects.nonNull(len) && len > 0) {
            this.numberLength = len;
        }
        final int limit = getLimit();
        String number1 = Integer.toString(RandomUtils.randomInt(limit));
        String number2 = Integer.toString(RandomUtils.randomInt(limit));
        number1 = Strings.padEnd(number1, this.numberLength, ' ');
        number2 = Strings.padEnd(number2, this.numberLength, ' ');
        return number1 + RandomUtils.randomChar(OPERATORS) + number2 + "=";
    }

    @Override
    public boolean verify(final String captchaCode, final String inputCode) {
        int result;
        try {
            result = Integer.parseInt(captchaCode);
        } catch (NumberFormatException e) {
            // 用户输入非数字
            return false;
        }
        final int calculateResult = (int) Calculator.conversion(captchaCode);
        return result == calculateResult;
    }

    /**
     * 获取验证码长度
     *
     * @return 验证码长度
     */
    @Override
    public int getLen() {
        return this.numberLength * 2 + 2;
    }

    /**
     * 根据长度获取参与计算数字最大值
     *
     * @return 最大值
     */
    private int getLimit() {
        return Integer.parseInt("1" + Strings.repeat("0", this.numberLength));
    }
}
