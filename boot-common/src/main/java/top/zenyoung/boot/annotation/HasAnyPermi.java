package top.zenyoung.boot.annotation;

import java.lang.annotation.*;

/**
 * 拥有任意一个权限-注解
 *
 * @author young
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasAnyPermi {
    /**
     * 获取权限集合
     *
     * @return 权限集合
     */
    String[] value();
}
