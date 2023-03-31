package top.zenyoung.netty.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

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
        final String val = Optional.of(LOCAL.get())
                .map(map -> map.getOrDefault(cls, null))
                .orElseGet(() -> Optional.ofNullable(cls.getAnnotation(Scope.class))
                        .map(Scope::value)
                        .map(scopeVal -> {
                            LOCAL.get().put(cls, Strings.isNullOrEmpty(scopeVal) ? SINGLETON : scopeVal);
                            return scopeVal;
                        })
                        .orElse(SINGLETON)
                );
        //检查值
        if (!PROTOTYPE.equalsIgnoreCase(val)) {
            throw new IllegalStateException(cls.getName() + "类必须注解@Scope(\"" + PROTOTYPE + "\")");
        }
    }
}
