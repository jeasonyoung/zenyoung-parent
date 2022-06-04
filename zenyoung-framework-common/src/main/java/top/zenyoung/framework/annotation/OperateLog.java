package top.zenyoung.framework.annotation;

import java.lang.annotation.*;

/**
 * 操作日志-注解
 *
 * @author young
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {
    /**
     * 操作名称
     *
     * @return 操作名称
     */
    String value() default "";
}
