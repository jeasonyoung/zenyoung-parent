package top.zenyoung.boot.annotation;

import java.lang.annotation.*;

/**
 * 操作日志回显-注解
 *
 * @author young
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperaLogView {
    /**
     * 获取请求参数回显字段名
     *
     * @return 回显字段名
     */
    String value();
}
