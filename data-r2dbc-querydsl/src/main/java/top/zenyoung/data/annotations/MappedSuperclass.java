package top.zenyoung.data.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A mapped superclass has no separate table defined for it.
 *
 * @author young
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface MappedSuperclass {

}
