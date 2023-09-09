package top.zenyoung.retrofit.retry;

import java.lang.annotation.*;

/**
 * 重试-注解
 *
 * @author young
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Retry {
    /**
     * 是否启用重试
     *
     * @return 是否启用重试
     */
    boolean enable() default true;

    /**
     * 最大重试次数,最大可设置为100
     *
     * @return 最大重试次数
     */
    int maxRetries() default 2;

    /**
     * 重试时间间隔(毫秒)
     *
     * @return 重试时间间隔
     */
    int intervalMs() default 100;

    /**
     * 重试规则，默认 响应状态码不是2xx 或者 发生IO异常 时触发重试
     *
     * @return 重试规则
     */
    RetryRule[] retryRules() default {RetryRule.RES_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXP};
}
