package top.zenyoung.framework.annotation;

import java.lang.annotation.*;

/**
 * 操作日志回显-注解
 *
 * @author young
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLogView {
    /**
     * 获取请求参数回显字段名
     *
     * @return 回显字段名
     */
    String value();
}
