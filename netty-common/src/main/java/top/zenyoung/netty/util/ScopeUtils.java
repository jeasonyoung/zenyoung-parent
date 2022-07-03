package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * Scope工具类
 *
 * @author young
 */
public class ScopeUtils {
    private static final String SINGLETON = "singleton";
    private static final String PROTOTYPE = "prototype";
    private static final ThreadLocal<Map<Class<?>, String>> LOCAL = ThreadLocal.withInitial(Maps::newConcurrentMap);

    public static void checkPrototype(@Nonnull final Class<?> cls) {
        String val = LOCAL.get().get(cls);
        if (Strings.isNullOrEmpty(val) && cls.isAnnotationPresent(Scope.class)) {
            final Scope scope = cls.getAnnotation(Scope.class);
            if (Objects.nonNull(scope)) {
                val = scope.value();
                LOCAL.get().put(cls, Strings.isNullOrEmpty(val) ? SINGLETON : val);
            }
        }
        //检查值
        if (!PROTOTYPE.equalsIgnoreCase(val)) {
            throw new IllegalStateException(cls.getName() + "类必须注解@Scope(\"" + PROTOTYPE + "\")");
        }
    }
}
