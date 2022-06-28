package top.zenyoung.boot.annotation;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.zenyoung.boot.registrar.BootRegistrar;

import java.lang.annotation.*;

/**
 * Boot启动注解
 *
 * @author young
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@EnableAsync
@EnableScheduling
@SpringBootApplication
@Import({BootRegistrar.class})
public @interface Boot {
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
    String[] scanBasePackages() default {};

    /**
     * 扫描基础包类
     *
     * @return 基础包类
     */
    @AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
    Class<?>[] scanbasePackageClasses() default {};
}
