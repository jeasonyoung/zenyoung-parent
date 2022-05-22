package top.zenyoung.framework.annotation;

import top.zenyoung.framework.common.BusinessType;

import java.lang.annotation.*;

/**
 * 操作日志记录
 *
 * @author young
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {
    /**
     * 模块
     */
    String title() default "";

    /**
     * 主键字段名
     *
     * @return 主键字段名
     */
    String primaryKey() default "id";

    /**
     * 功能
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveReqData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveRespData() default true;
}
