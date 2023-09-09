package top.zenyoung.retrofit.core;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import top.zenyoung.retrofit.FallbackFactory;
import top.zenyoung.retrofit.util.ContextUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 降级工厂代理
 *
 * @author young
 */
@RequiredArgsConstructor
public class FallbackFactoryProxy implements InvocationHandler {
    private final Object source;
    private final FallbackFactory<?> fallbackFactory;

    @SuppressWarnings({"unchecked"})
    public static <T> T create(@Nonnull final Class<T> retrofitInterface, @Nonnull final Class<FallbackFactory<?>> fallbackFactoryClass,
                               @Nonnull final Object source, @Nonnull final ApplicationContext context) {
        final FallbackFactory<?> factory = ContextUtils.getBeanOrNew(context, fallbackFactoryClass);
        final FallbackFactoryProxy factoryProxy = new FallbackFactoryProxy(source, factory);
        return (T) Proxy.newProxyInstance(
                retrofitInterface.getClassLoader(),
                new Class<?>[]{retrofitInterface},
                factoryProxy
        );
    }

    @Override
    public Object invoke(@Nullable final Object proxy, @Nonnull final Method method, @Nullable final Object[] args) throws Throwable {
        try {
            return method.invoke(source, args);
        } catch (Exception e) {
            final Throwable cause = e.getCause();
            if (Objects.nonNull(cause) && Objects.nonNull(fallbackFactory)) {
                final Object fallbackObject = fallbackFactory.create(cause);
                return method.invoke(fallbackObject, args);
            }
            return cause;
        }
    }
}
