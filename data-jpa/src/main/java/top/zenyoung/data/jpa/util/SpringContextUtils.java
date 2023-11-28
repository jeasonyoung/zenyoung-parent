package top.zenyoung.data.jpa.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spring 上下文工具类
 *
 * @author young
 */
@Slf4j
@UtilityClass
public class SpringContextUtils {
    private static final AtomicReference<ApplicationContext> refCtx = new AtomicReference<>(null);

    public static void setContext(@Nonnull final ApplicationContext ctx) {
        refCtx.set(ctx);
    }

    public static ApplicationContext getContext() {
        return refCtx.get();
    }

    public static <T> T getBean(@Nonnull final Class<T> beanCls) {
        return Optional.ofNullable(getContext())
                .map(ctx -> {
                    try {
                        return ctx.getBean(beanCls);
                    } catch (BeansException e) {
                        log.warn("getBean(beanCls: {})-exp: {}", beanCls, e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }
}
