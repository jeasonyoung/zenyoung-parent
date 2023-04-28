package top.zenyoung.boot.annotation;

import top.zenyoung.common.model.OperaType;

import java.lang.annotation.*;

/**
 * 操作日志记录
 *
 * @author young
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperaLog {
    /**
     * 获取模块标题
     *
     * @return 模块标题
     */
    String title() default "";

    /**
     * 主键字段名
     *
     * @return 主键字段名
     */
    String primaryKey() default "id";

    /**
     * 获取功能类型
     *
     * @return 功能类型
     */
    OperaType operaType() default OperaType.Other;

    /**
     * 是否保存请求的参数
     *
     * @return 是否保存请求参数
     */
    boolean isSaveReqData() default true;

    /**
     * 是否保存响应的参数
     *
     * @return 是否保存响应
     */
    boolean isSaveRespData() default true;
}
