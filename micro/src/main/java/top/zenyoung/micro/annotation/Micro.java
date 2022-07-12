package top.zenyoung.micro.annotation;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AliasFor;
import top.zenyoung.boot.annotation.Boot;

import java.lang.annotation.*;

/**
 * 微服务-注解
 *
 * @author young
 */
@Boot
@Inherited
@Documented
@EnableFeignClients
@EnableDiscoveryClient
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Micro {
    /**
     * 排除启动服务集合
     *
     * @return 排除集合
     */
    @AliasFor(annotation = EnableAutoConfiguration.class)
    Class<?>[] exclude() default {};

    /**
     * 排除启动服务集合
     *
     * @return 排除结合
     */
    @AliasFor(annotation = EnableAutoConfiguration.class)
    String[] excludeName() default {};

    /**
     * 扫描基础包名
     *
     * @return 基础包名
     */
    @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
    String[] basePackages() default {};

    /**
     * 扫描基础包类
     *
     * @return 基础包类
     */
    @AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
    Class<?>[] basePackageClasses() default {};

    /**
     * 扫描FeignClient包名
     *
     * @return FeignClient包名
     */
    @AliasFor(annotation = EnableFeignClients.class, attribute = "basePackages")
    String[] feignBasePackages() default {};

    /**
     * 扫描FeignClient包类
     *
     * @return FeignClient包类
     */
    @AliasFor(annotation = EnableFeignClients.class, attribute = "basePackageClasses")
    Class<?>[] feignBasePackageClasses() default {};
}
