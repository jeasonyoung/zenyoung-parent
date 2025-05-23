package top.zenyoung.boot.aop;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

/**
 * 切面处理基类
 *
 * @author young
 */
@Slf4j
public abstract class BaseAspect {
    private static final Class<?>[] PRIMITIVES = new Class<?>[]{
            String.class,
            Number.class,
            BigDecimal.class,
            Date.class,
            LocalDate.class,
            LocalTime.class
    };

    /**
     * 检查是否为基本类型
     *
     * @param cls Class
     * @return 是否为基本类型
     */
    protected static boolean isPrimitive(@Nonnull final Class<?> cls) {
        try {
            if (ClassUtils.isPrimitiveOrWrapper(cls)) {
                return true;
            }
            //自定义基本类型
            for (Class<?> pc : PRIMITIVES) {
                if (pc.isAssignableFrom(cls)) {
                    return true;
                }
            }
            //8种值类型的包装对象类型判断
            final Object obj = cls.getField("TYPE").get(null);
            if (obj instanceof Class) {
                return ((Class<?>) obj).isPrimitive();
            }
        } catch (NoSuchFieldException ex) {
            return false;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            log.warn("isPrimitive(cls: {})-exp: {}", cls, ex.getMessage());
        }
        return false;
    }

    /**
     * 从Map中递归搜索数据
     *
     * @param sourceMap 搜索的源Map
     * @param key       需要搜索的目标字段
     * @return 搜索数据
     */
    @SuppressWarnings({"unchecked"})
    protected static Object recursionSearch(@Nonnull final Map<String, Object> sourceMap, @Nonnull final String key) {
        if (!CollectionUtils.isEmpty(sourceMap) && !Strings.isNullOrEmpty(key)) {
            Object val = sourceMap.getOrDefault(key, null);
            if (val != null) {
                return val;
            }
            for (Object v : sourceMap.values()) {
                if (v instanceof Map) {
                    val = recursionSearch((Map<String, Object>) v, key);
                    if (val != null) {
                        return val;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取切面参数处理
     *
     * @param joinPoint 切面
     * @param handler   参数处理
     * @return 处理结果
     */
    protected static List<String> getReqArgs(@Nonnull final JoinPoint joinPoint, @Nonnull final Function<Object, String> handler) {
        final List<String> arguments = Lists.newLinkedList();
        final Object[] args = joinPoint.getArgs();
        if (Objects.nonNull(args)) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                if ((arg instanceof ServerHttpRequest) || (arg instanceof ServerHttpResponse) || (arg instanceof MultipartFile)) {
                    continue;
                }
                final String ret = handler.apply(arg);
                if (!Strings.isNullOrEmpty(ret)) {
                    arguments.add(ret);
                }
            }
        }
        return arguments;
    }

    private static Optional<Method> getMethod(@Nonnull final JoinPoint joinPoint) {
        return Optional.ofNullable(joinPoint.getSignature())
                .filter(MethodSignature.class::isInstance)
                .map(MethodSignature.class::cast)
                .map(MethodSignature::getMethod);
    }

    public static <A extends Annotation> boolean hasMethodAnnotation(@Nonnull final JoinPoint joinPoint, @Nonnull final Class<A> annotationType) {
        return getMethod(joinPoint)
                .map(method -> AnnotatedElementUtils.hasAnnotation(method, annotationType))
                .orElse(false);
    }

    public static <A extends Annotation> A getMethodAnnotation(@Nonnull final JoinPoint joinPoint, @Nonnull final Class<A> annotationType) {
        return getMethod(joinPoint)
                .map(method -> AnnotatedElementUtils.findMergedAnnotation(method, annotationType))
                .orElse(null);
    }
}
