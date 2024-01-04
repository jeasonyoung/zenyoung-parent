package top.zenyoung.boot.annotation;

import java.lang.annotation.*;

/**
 * 隐私信息保护输出
 *
 * @author young
 */
@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivacyPolicy {
    /**
     * 脱敏-手机号码字段
     *
     * @return 手机号码字段
     */
    String[] mobiles() default {};

    /**
     * 脱敏-身份证号码字段
     *
     * @return 身份证号码字段
     */
    String[] idCards() default {};

    /**
     * 脱敏类型
     *
     * @return 策略类型
     */
    PrivacyPolicyType policy() default PrivacyPolicyType.NULL;
}
