package top.zenyoung.boot.annotation;

import java.lang.annotation.*;

/**
 * 防止表单重复提交
 *
 * @author young
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {
    /**
     * 间隔时间,小于此时间间隔视为重复提交
     *
     * @return 间隔时间
     */
    String duration() default "PT10S";

    /**
     * 提示消息
     */
    String message() default "不允许重复提交，请稍后再试";
}
