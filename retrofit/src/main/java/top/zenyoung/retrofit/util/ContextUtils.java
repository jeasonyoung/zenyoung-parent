package top.zenyoung.retrofit.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import top.zenyoung.retrofit.exception.RetrofitException;

import javax.annotation.Nonnull;

/**
 * 上下文工具类
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class ContextUtils {

    @SuppressWarnings({"unchecked"})
    public static <T> T getBeanOrNew(@Nonnull final ApplicationContext context, @Nonnull final Class<T> cls) {
        try {
            return context.getBean(cls);
        } catch (Exception e1) {
            log.warn("Failed to get bean from applicationContext.", e1);
            try {
                return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e2) {
                log.warn("Failed to create instance by reflection.", e2);
                try {
                    return (T) cls.getMethod("create").invoke(null);
                } catch (Exception e3) {
                    throw new RetrofitException("Failed to create instance through create static method.", e3);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getTargetInstanceIfNecessary(@Nonnull final T bean) {
        Object obj = bean;
        while (AopUtils.isAopProxy(obj)) {
            try {
                obj = ((Advised) obj).getTargetSource().getTarget();
            } catch (Exception e) {
                log.warn("Failed to get target source.", e);
            }
        }
        return (T) obj;
    }
}
