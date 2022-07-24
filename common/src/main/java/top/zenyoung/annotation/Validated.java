package top.zenyoung.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 请求验证器封装
 *
 * @author young
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@org.springframework.validation.annotation.Validated
public @interface Validated {
    /**
     * 用于验证分组
     *
     * @return 验证分组
     */
    @AliasFor(annotation = org.springframework.validation.annotation.Validated.class, attribute = "value")
    Class<?>[] value() default {};
}
