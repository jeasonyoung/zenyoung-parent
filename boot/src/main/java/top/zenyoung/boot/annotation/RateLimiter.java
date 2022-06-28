package top.zenyoung.boot.annotation;

import top.zenyoung.boot.model.LimitPolicy;

import java.lang.annotation.*;

/**
 * 请求限流
 *
 * @author young
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    /**
     * 限流键(默认为空,同一限流键共享同一限流策略)
     *
     * @return 限流键
     */
    String key() default "";

    /**
     * 限流时间(单位:秒)
     *
     * @return 限流时间
     */
    int time() default 10;

    /**
     * 限流次数(限流时间内允许访问的次数上限)
     *
     * @return 限流次数
     */
    int max() default 100;

    /**
     * 限流策略(默认全局)
     *
     * @return 限流策略
     */
    LimitPolicy policy() default LimitPolicy.Global;
}
