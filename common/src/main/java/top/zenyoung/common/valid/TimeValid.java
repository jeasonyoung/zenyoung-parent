package top.zenyoung.common.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 时间校验
 *
 * @author young
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Constraint(validatedBy = TimeValidator.class)
public @interface TimeValid {
    boolean checkEmpty() default false;

    String message() default "时间格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
