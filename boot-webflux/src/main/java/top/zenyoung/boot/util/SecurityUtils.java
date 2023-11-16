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

    public static Mono<UserPrincipal> getContext() {
        return Mono.deferContextual(ctx -> {
            if (hasUserPrincipalContext(ctx)) {
                return getUserPrincipalContext(ctx);
            }
            return Mono.empty();
        });
    }

    private static boolean hasUserPrincipalContext(@Nonnull final ContextView context) {
        return context.hasKey(SECURITY_CONTEXT_KEY);
    }

    private static Mono<UserPrincipal> getUserPrincipalContext(@Nonnull final ContextView context) {
        return context.<Mono<UserPrincipal>>get(SECURITY_CONTEXT_KEY);
    }

    public static Context withUserPrincipalContext(@Nonnull final Mono<? extends UserPrincipal> userPrincipal) {
        return Context.of(SECURITY_CONTEXT_KEY, userPrincipal);
    }

    public static Context withUserPrincipal(@Nonnull final UserPrincipal userPrincipal) {
        return withUserPrincipalContext(Mono.just(userPrincipal));
    }
}
