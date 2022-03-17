package top.zenyoung.framework.annotation;

import top.zenyoung.framework.common.PrivacyPolicyType;

import java.lang.annotation.*;

/**
 * 隐私信息保护输出
 *
 * @author young
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrivacyPolicy {
    /**
     * 获取隐私保护字段数组(注解在方法上生效)
     *
     * @return 隐私保护字段数组
     */
    String[] fields() default "";

    /**
     * 隐私保护策略类型
     *
     * @return 策略类型
     */
    PrivacyPolicyType policy() default PrivacyPolicyType.Mobile;
}
