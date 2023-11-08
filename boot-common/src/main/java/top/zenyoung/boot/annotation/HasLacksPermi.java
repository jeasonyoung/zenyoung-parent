package top.zenyoung.boot.annotation;

import java.lang.annotation.*;

/**
 * 不拥有某权限(与HasPermi逻辑相反)-注解
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasLacksPermi {
    /**
     * 获取权限值
     *
     * @return 权限值
     */
    String value();
}
