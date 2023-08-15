package top.zenyoung.graphics.captcha.generator;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import top.zenyoung.graphics.captcha.Calculator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 数字计算验证码生成器
 *
 * @author young
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
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
    public String generate(@Nullable final Integer len) {
        if (Objects.nonNull(len) && len > 0) {
            this.numberLength = len;
        }
        final int limit = getLimit();
        String number1 = Integer.toString(randomInt(limit));
        String number2 = Integer.toString(randomInt(limit));
        number1 = Strings.padEnd(number1, this.numberLength, ' ');
        number2 = Strings.padEnd(number2, this.numberLength, ' ');
        return number1 + randomChar(OPERATORS) + number2 + "=";
    }

    @Override
    public boolean verify(@Nonnull final String captchaCode, @Nonnull final String inputCode) {
        int result;
        try {
            result = Integer.parseInt(inputCode);
        } catch (NumberFormatException e) {
            log.error("verify(captchaCode: {},inputCode: {})[inputCode: {}]-exp: {}", captchaCode, inputCode, inputCode, e.getMessage());
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
