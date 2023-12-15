package top.zenyoung.boot.annotation.authority;

import java.lang.annotation.*;

/**
 * 拥有某个角色-注解
 *
 * @author young
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasRole {
    /**
     * 获取角色
     *
     * @return 角色值
     */
    String value();
}
