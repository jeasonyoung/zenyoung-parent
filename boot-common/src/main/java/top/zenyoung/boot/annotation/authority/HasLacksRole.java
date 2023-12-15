package top.zenyoung.boot.annotation.authority;

import java.lang.annotation.*;

/**
 * 不拥有某角色(与HasRole逻辑相反)-注解
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasLacksRole {
    /**
     * 获取角色
     *
     * @return 角色值
     */
    String value();
}
