package top.zenyoung.orm.annotation;

import top.zenyoung.orm.enums.DbField;

import java.lang.annotation.*;

/**
 * 实体类字段注解
 *
 * @author young
 */
@Inherited
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PoField {
    /**
     * 表字段填充
     *
     * @return 表字段
     */
    DbField fill() default DbField.DEFAULT;
}