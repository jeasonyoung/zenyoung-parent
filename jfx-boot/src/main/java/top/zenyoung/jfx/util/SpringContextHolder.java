package top.zenyoung.jfx.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Spring上下文Holder
 *
 * @author young
 */
public class SpringContextHolder implements ApplicationContextAware {
    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        setContext(context);
    }

    public static void setContext(@Nonnull final ApplicationContext context) {
        ctx = context;
    }

    public static void contextHandler(@Nonnull final Consumer<ApplicationContext> handler) {
        Optional.ofNullable(ctx)
                .ifPresent(handler);
    }

    public static <T> T getBean(@Nonnull final Class<T> beanClass) {
        return Optional.ofNullable(ctx)
                .map(c -> c.getBean(beanClass))
                .orElse(null);
    }
}
