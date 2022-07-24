package top.zenyoung.annotation;

import java.lang.annotation.*;

/**
 * mybatisplus枚举封装
 *
 * @author young
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@com.baomidou.mybatisplus.annotation.EnumValue
public @interface DbEnumValue {

}
