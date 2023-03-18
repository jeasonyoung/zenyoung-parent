package top.zenyoung.boot.annotation;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
import top.zenyoung.boot.registrar.BootRegistrar;

import java.lang.annotation.*;

/**
 * Boot启动注解
 *
 * @author young
 */
@Inherited
@Documented
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableSwagger2WebMvc
@SpringBootApplication
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({BootRegistrar.class})
public @interface Boot {
    /**
     * 排除启动服务集合
     *
     * @return 排除集合
     */
    @AliasFor(annotation = EnableAutoConfiguration.class, attribute = "exclude")
    Class<?>[] exclude() default {};

    /**
     * 排除启动服务集合
     *
     * @return 排除结合
     */
    @AliasFor(annotation = EnableAutoConfiguration.class, attribute = "excludeName")
    String[] excludeName() default {};

    /**
     * 扫描基础包名
     *
     * @return 基础包名
     */
    @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
    String[] scanBasePackages() default {"top.zenyoung"};

    /**
     * 扫描基础包类
     *
     * @return 基础包类
     */
    @AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
    Class<?>[] scanBasePackageClasses() default {};
}
