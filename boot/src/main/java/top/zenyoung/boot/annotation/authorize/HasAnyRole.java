package top.zenyoung.boot.annotation.authorize;

import java.lang.annotation.*;

/**
 * 拥有任意一个角色-注解
 *
 * @author young
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasAnyRole {
    /**
     * 获取角色集合
     *
     * @return 角色集合
     */
    String[] value();
}
