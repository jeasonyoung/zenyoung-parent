package top.zenyoung.framework.runtime.aspectj;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

/**
 * 切面处理基类
 *
 * @author young
 */
@Slf4j
abstract class BaseAspect {

    private final static Class<?>[] PRIMITIVES = new Class<?>[]{
            String.class, Number.class, BigDecimal.class,
            Date.class, LocalDate.class, LocalTime.class
    };

    /**
     * 检查是否为基本类型
     *
     * @param cls Class
     * @return 是否为基本类型
     */
    protected boolean isPrimitive(@Nonnull final Class<?> cls) {
        try {
            //8种值类型直接判断
            if (cls.isPrimitive()) {
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
        } catch (Throwable ex) {
            log.warn("isPrimitive(cls: {})-exp: {}", cls, ex.getMessage());
        }
        return false;
    }
}
