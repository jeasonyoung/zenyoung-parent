package top.zenyoung.boot.annotation.authorize;

import java.lang.annotation.*;

/**
 * 拥有某个权限-注解
 *
 * @author young
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasPermi {
    /**
     * 获取权限值
     *
     * @return 权限值
     */
    String value() default "*:*:*";
}
