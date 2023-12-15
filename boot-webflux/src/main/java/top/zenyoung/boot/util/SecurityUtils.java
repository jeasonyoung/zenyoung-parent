package top.zenyoung.boot.util;

import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;
import top.zenyoung.common.model.UserPrincipal;

import javax.annotation.Nonnull;

/**
 * 安全服务工具类
 *
 * @author young
 */
@UtilityClass
public class SecurityUtils {
    private static final Class<?> SECURITY_CONTEXT_KEY = UserPrincipal.class;

    public static Mono<UserPrincipal> getPrincipal(@Nonnull final ContextView contextView) {
        final UserPrincipal principal = contextView.getOrDefault(SECURITY_CONTEXT_KEY, null);
        return Mono.justOrEmpty(principal);
    }

    public static Mono<UserPrincipal> getPrincipal() {
        return Mono.deferContextual(SecurityUtils::getPrincipal);
    }

    public static Context withPrincipal(@Nonnull final Context context, @Nonnull final UserPrincipal principal) {
        return context.put(SECURITY_CONTEXT_KEY, principal);
    }
}
