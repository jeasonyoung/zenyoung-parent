package top.zenyoung.framework.annotation;

import top.zenyoung.framework.common.LogViewFieldType;

import java.lang.annotation.*;

/**
 * 日志字段回显处理
 *
 * @author young
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLogViewFieldValue {
    /**
     * 回显字段类型
     *
     * @return 字段类型
     */
    LogViewFieldType type() default LogViewFieldType.Biz;

    /**
     * 处理Bean类名
     *
     * @return Bean类名
     */
    Class<?> beanClass() default Void.class;

    /**
     * 方法名
     *
     * @return 方法名
     */
    String method() default "";

    /**
     * 字典类型名称
     *
     * @return 字典类型名称
     */
    String dictTypeName() default "";

    /**
     * 是否缓存
     *
     * @return 是否缓存
     */
    boolean cache() default true;
}