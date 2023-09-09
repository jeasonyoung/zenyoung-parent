package top.zenyoung.retrofit.annotation;

import org.springframework.context.annotation.Import;
import top.zenyoung.retrofit.core.RetrofitClientScannerRegistrar;

import java.lang.annotation.*;

/**
 * Retrofit-客户端扫描_注解
 *
 * @author young
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RetrofitClientScannerRegistrar.class)
public @interface RetrofitClientScan {
    /**
     * Scan package path
     *
     * @return basePackages
     */
    String[] basePackages() default {};

    /**
     * Scan package classes
     *
     * @return Scan package classes
     */
    Class<?>[] basePackageClasses() default {};
}
