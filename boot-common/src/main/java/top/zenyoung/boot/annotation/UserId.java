package top.zenyoung.boot.annotation;

import java.lang.annotation.*;

/**
 * 认证用户ID
 *
 * @author young
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserId {

}
