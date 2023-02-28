package top.zenyoung.boot.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Knife4j判断条件
 *
 * @author young
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(value = ConditionalOnKnife4jEnable.ENABLED_KEY, matchIfMissing = true, havingValue = "true")
public @interface ConditionalOnKnife4jEnable {
    String ENABLED_KEY = "top.zenyoung.swagger.knife4j";
}
