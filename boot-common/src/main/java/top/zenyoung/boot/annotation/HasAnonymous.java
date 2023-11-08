package top.zenyoung.boot.annotation;

import java.lang.annotation.*;

/**
 * 匿名访问(不鉴权)-注解
 *
 * @author young
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasAnonymous {

}
