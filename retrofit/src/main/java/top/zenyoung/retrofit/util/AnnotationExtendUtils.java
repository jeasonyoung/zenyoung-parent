package top.zenyoung.retrofit.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 注解扩展-工具类
 *
 * @author young
 */
@UtilityClass
public class AnnotationExtendUtils {
    /**
     * 查找方法及其类上的指定注解，优先返回方法上的。
     *
     * @param <A>            注解泛型参数
     * @param method         方法
     * @param clazz          类型
     * @param annotationType 注解类型
     * @return 方法及其类上的指定注解。
     */
    public static <A extends Annotation> A findMergedAnnotation(@Nonnull final Method method, @Nonnull final Class<?> clazz, @Nonnull final Class<A> annotationType) {
        final A annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (annotation != null) {
            return annotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(clazz, annotationType);
    }

    /**
     * 判断某个类及其公有方法上是否存在指定注解。
     *
     * @param <A>            注解泛型参数
     * @param clazz          类
     * @param annotationType 注解类型
     * @return 判断某个类及其公有方法上是否存在指定注解。
     */
    public static <A extends Annotation> boolean isAnnotationPresentIncludeMethod(@Nonnull final Class<?> clazz, @Nonnull final Class<A> annotationType) {
        if (AnnotatedElementUtils.findMergedAnnotation(clazz, annotationType) != null) {
            return true;
        }
        for (Method method : clazz.getMethods()) {
            if (AnnotatedElementUtils.findMergedAnnotation(method, annotationType) != null) {
                return true;
            }
        }
        return false;
    }
}
