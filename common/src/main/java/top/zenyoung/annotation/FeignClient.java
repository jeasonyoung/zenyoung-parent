package top.zenyoung.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * FeignClient封装
 *
 * @author young
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@org.springframework.cloud.openfeign.FeignClient
public @interface FeignClient {
    /**
     * 调用微服务名
     *
     * @return 微服务名
     */
    @AliasFor(annotation = org.springframework.cloud.openfeign.FeignClient.class, attribute = "name")
    String name() default "";

    /**
     * 调用服务路径
     *
     * @return 调用服务路径
     */
    @AliasFor(annotation = org.springframework.cloud.openfeign.FeignClient.class, attribute = "path")
    String path() default "";

    /**
     * 熔断调用类
     *
     * @return 熔断调用类
     */
    @AliasFor(annotation = org.springframework.cloud.openfeign.FeignClient.class, attribute = "fallback")
    Class<?> fallback() default void.class;
}
